"""Regression tests for the deliberately narrow, fail-closed filter converter."""
import gzip
import hashlib
import json
import unittest
from unittest.mock import patch
from scripts import update_adblock_filters as f


class AdblockFilterTests(unittest.TestCase):
    def test_canonical_gzip_and_safe_rule_selection(self):
        source = "\n".join((
            "||ads.example.com^",
            "||ads.example.com^",  # dedupe
            "||billing.stripe.com^",  # protected
            "||news.example.com/path^",  # path excluded
            "||restricted.example.com^$third-party",  # option excluded
            "||*.invalid.com^",  # wildcard excluded
            "@@||ads.example.com^",  # exception always wins
            "||cdn.adserver.com^",
            "##.adsbygoogle",
            "example.com##.advertisement",  # domain-scoped excluded
            "##.unsafe-custom-selector",  # unapproved excluded
        ))
        with patch.object(f, "MIN_HOSTS", 1):
            document, gz = f.convert(
                [source], {".adsbygoogle"}, {"stripe.com"}, "2026-11-20T00:00:00Z"
            )
        self.assertEqual(document["hosts"], ["cdn.adserver.com"])
        self.assertEqual(document["exceptions"], ["ads.example.com"])
        self.assertEqual(document["css"], ".adsbygoogle{display:none!important}")
        self.assertEqual(json.loads(gzip.decompress(gz)), document)
        expected = hashlib.sha256(
            f.canonical(document["version"], document["generated_at"],
                        set(document["hosts"]), set(document["exceptions"]),
                        document["css"]).encode("utf-8")
        ).hexdigest()
        self.assertEqual(document["sha256"], expected)
        self.assertLessEqual(len(gz), 2 * 1024 * 1024)

    def test_abnormal_host_count_refuses_publication(self):
        with self.assertRaisesRegex(ValueError, "unsafe host count"):
            f.convert(["||ads.example.com^"], set(), set(), "2026-11-20T00:00:00Z")

    def test_host_boundary(self):
        self.assertTrue(f.protected("login.stripe.com", {"stripe.com"}))
        self.assertFalse(f.protected("fake-stripe.com", {"stripe.com"}))
        self.assertFalse(f.HOST.fullmatch("bad..example.com"))
        self.assertIsNone(f.DOMAIN_RULE.fullmatch("||example.com^$popup"))


if __name__ == "__main__":
    unittest.main()
