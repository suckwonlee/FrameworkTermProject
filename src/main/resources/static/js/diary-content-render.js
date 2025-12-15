document.addEventListener("DOMContentLoaded", function () {
    var raw = document.getElementById("diary-raw-content");
    var root = document.getElementById("diary-render-root");

    if (!raw || !root) {
        return;
    }

    var content = raw.value || "";
    raw.parentNode.removeChild(raw);

    root.innerHTML = "";

    var view = document.createElement("div");
    view.className = "diary-lined-view diary-lined-block";

    view.style.backgroundPosition = "0 4px";
    view.style.wordBreak = "break-word";

    var tokenRegex = /\[\[img:([^\]]+)\]\]/g;

    var endsWithNewline = true;

    function appendText(text) {
        if (!text) {
            return;
        }
        view.appendChild(document.createTextNode(text));
        endsWithNewline = text.endsWith("\n");
    }

    function appendNewlineIfNeeded() {
        if (!endsWithNewline) {
            view.appendChild(document.createTextNode("\n"));
            endsWithNewline = true;
        }
    }

    function appendImage(url) {
        if (!url) {
            return;
        }

        // 이미지가 텍스트에 바짝 붙지 않게 최소 개행
        appendNewlineIfNeeded();

        var img = document.createElement("img");
        img.className = "diary-inline-image";
        img.src = url;
        img.alt = "본문 이미지";

        img.style.display = "block";
        img.style.margin = "14px auto";
        img.style.maxWidth = "min(760px, 100%)";
        img.style.height = "auto";
        img.style.borderRadius = "16px";

        // 배경 박스 없이, 줄이 이미지에 붙어 보이는 느낌만 완화(얇은 헤일로)
        img.style.boxShadow =
            "0 10px 24px rgba(15, 23, 42, 0.16), 0 0 0 8px rgba(255, 255, 255, 0.92)";

        view.appendChild(img);

        // 이미지 뒤도 최소 개행
        view.appendChild(document.createTextNode("\n"));
        endsWithNewline = true;
    }

    var lastIndex = 0;
    var match;

    while ((match = tokenRegex.exec(content)) !== null) {
        var start = match.index;
        var end = tokenRegex.lastIndex;

        var before = content.substring(lastIndex, start);
        appendText(before);

        appendImage(match[1]);

        lastIndex = end;
    }

    appendText(content.substring(lastIndex));

    root.appendChild(view);
});
