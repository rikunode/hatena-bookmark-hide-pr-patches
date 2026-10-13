package dev.rikunode.hatenabookmark

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

/**
 * Verified against Hatena Bookmark 5.80 / versionCode 58000 DEX (static extraction):
 *
 * MuteHelperKt.filteredWithMuteWords(BookmarkEntry, List): Boolean
 * calls BookmarkEntry.isPrEntry() at DEX code-unit offset 21.
 * If that is true it unconditionally returns true ("keep item"),
 * bypassing the normal mute filter.
 *
 * For PR entries only, return false at the top. For every other entry,
 * fall through to the original filtering method without modification.
 *
 * NOTE: This affects feeds that call filterByMuteWords. Actual device
 * confirmation and coverage of all feed types remain pending.
 */
private object NativeAdFilterFingerprint : Fingerprint(
    definingClass = "Lcom/hatena/android/bookmark/interactor/MuteHelperKt;",
    name = "filteredWithMuteWords",
    parameters = listOf(
        "Lcom/hatena/android/bookmark/model/BookmarkEntry;",
        "Ljava/util/List;",
    ),
    returnType = "Z",
)

@Suppress("unused")
val hideNativePrEntriesPatch = bytecodePatch(
    name = "Hide native PR entries from article feeds",
    description = "Filter PR entries before Hatena's mute-word logic. Keeps ordinary articles and their original mute behavior.",
    default = true,
) {
    compatibleWith(
        Compatibility(
            name = "Hatena Bookmark",
            packageName = "com.hatena.android.bookmark",
            apkFileType = ApkFileType.APK,
            appIconColor = 0x2A9BD6,
            targets = listOf(AppTarget(version = "5.80", versionCode = 58000)),
        )
    )

    execute {
        val method = NativeAdFilterFingerprint.method
        val impl = requireNotNull(method.implementation) { "Mute filter has no bytecode" }
        val instructions = impl.instructions
        require(impl.registerCount == 8) {
            "Unexpected register layout in Hatena 5.80 mute filter"
        }
        require(instructions.sumOf { it.codeUnits } == 105) {
            "Unexpected Hatena 5.80 mute filter code size"
        }
        require(instructions.first().opcode == Opcode.INVOKE_VIRTUAL) {
            "Unexpected first instruction in Hatena mute filter"
        }
        val prCalls = instructions.withIndex().filter { (_, insn) ->
            insn.opcode == Opcode.INVOKE_VIRTUAL &&
                (insn as? ReferenceInstruction)?.reference?.toString() ==
                    "Lcom/hatena/android/bookmark/model/BookmarkEntry;->isPrEntry()Z"
        }
        require(prCalls.size == 1) {
            "Expected exactly one pre-existing PR classification call"
        }
        require(instructions.take(prCalls.single().index).sumOf { it.codeUnits } == 21) {
            "Hatena 5.80 PR classification moved; refusing unsafe patch"
        }

        // p0 is the BookmarkEntry. v0 is a scratch local register;
        // 5.80's original method overwrites v0 before it is read.
        method.addInstructionsWithLabels(
            0,
            """
                invoke-virtual { p0 }, Lcom/hatena/android/bookmark/model/BookmarkEntry;->isPrEntry()Z
                move-result v0
                if-eqz v0, :normal_article
                const/4 v0, 0x0
                return v0
                :normal_article
                nop
            """.trimIndent(),
        )
    }
}
