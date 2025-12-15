document.addEventListener("DOMContentLoaded", function () {
    var cards = document.querySelectorAll(".diary-library-card");
    if (!cards || cards.length === 0) {
        return;
    }

    var tokenRegexGlobal = /\[\[img:([^\]]+)\]\]/g;
    var tokenRegexFirst = /\[\[img:([^\]]+)\]\]/;

    cards.forEach(function (card) {
        var textEl = card.querySelector(".diary-library-text");
        if (!textEl) {
            return;
        }

        var rawEl = card.querySelector(".diary-content-raw");
        var raw = "";

        if (rawEl && typeof rawEl.value === "string") {
            raw = rawEl.value;
        } else {
            raw = textEl.textContent || "";
        }

        // 인라인 이미지 첫 URL(썸네일 용)
        var firstMatch = tokenRegexFirst.exec(raw);
        var inlineUrl = firstMatch ? firstMatch[1] : "";

        // ✅ 토큰을 완전히 제거 (요청: [이미지]도 안 보이게)
        var cleaned = raw.replace(tokenRegexGlobal, "");
        cleaned = cleaned.replace(/\s+/g, " ").trim();

        if (cleaned.length > 120) {
            cleaned = cleaned.substring(0, 120) + "…";
        }

        if (cleaned.length === 0) {
            cleaned = "(내용 없음)";
        }

        textEl.textContent = cleaned;

        // ✅ “첨부 이미지(기존 1장 업로드)”만 체크 (인라인 썸네일은 제외)
        var attachedThumbImg = card.querySelector(
            ".diary-library-thumb-wrapper:not(.diary-inline-thumb-wrapper) img"
        );

        var inlineThumbWrap = card.querySelector(".diary-inline-thumb-wrapper");
        var inlineThumbImg = inlineThumbWrap ? inlineThumbWrap.querySelector("img") : null;

        if (!attachedThumbImg && inlineThumbWrap && inlineThumbImg && inlineUrl) {
            inlineThumbImg.src = inlineUrl;
            inlineThumbWrap.style.display = "block";
        }
    });
});
