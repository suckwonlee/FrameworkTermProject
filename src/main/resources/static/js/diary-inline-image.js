document.addEventListener("DOMContentLoaded", function () {
    var tools = document.querySelectorAll(".inline-image-tool");
    if (!tools || tools.length === 0) {
        return;
    }

    var TOKEN_GLOBAL = /\[\[img:([^\]]+)\]\]/g;
    var TOKEN_FIRST = /\[\[img:([^\]]+)\]\]/;

    function getCsrfHeaders() {
        var tokenMeta = document.querySelector('meta[name="_csrf"]');
        var headerMeta = document.querySelector('meta[name="_csrf_header"]');

        if (!tokenMeta || !headerMeta) {
            return null;
        }

        var token = tokenMeta.getAttribute("content");
        var headerName = headerMeta.getAttribute("content");

        if (!token || !headerName) {
            return null;
        }

        var headers = {};
        headers[headerName] = token;
        return headers;
    }

    function uploadImage(uploadUrl, file) {
        var formData = new FormData();
        formData.append("image", file, file.name || "image.png");

        var csrfHeaders = getCsrfHeaders();
        var options = {
            method: "POST",
            body: formData
        };

        if (csrfHeaders) {
            options.headers = csrfHeaders;
        }

        return fetch(uploadUrl, options)
            .then(function (res) {
                if (!res.ok) {
                    throw new Error("upload failed");
                }
                return res.json();
            })
            .then(function (data) {
                if (!data || !data.url) {
                    throw new Error("invalid response");
                }
                return data.url;
            });
    }

    function appendTextWithBreaks(container, text) {
        var parts = String(text).split("\n");
        for (var i = 0; i < parts.length; i++) {
            if (parts[i].length > 0) {
                container.appendChild(document.createTextNode(parts[i]));
            }
            if (i !== parts.length - 1) {
                container.appendChild(document.createElement("br"));
            }
        }
    }

    function parseTokensToEditor(editor, rawText) {
        editor.innerHTML = "";

        var raw = rawText || "";
        var lastIndex = 0;
        var match;

        while ((match = TOKEN_GLOBAL.exec(raw)) !== null) {
            var start = match.index;
            var end = TOKEN_GLOBAL.lastIndex;

            var before = raw.substring(lastIndex, start);
            if (before && before.length > 0) {
                appendTextWithBreaks(editor, before);
            }

            var url = match[1];
            if (url) {
                var img = document.createElement("img");
                img.src = url;
                img.alt = "본문 이미지";
                img.setAttribute("data-inline-img", "1");
                img.setAttribute("data-url", url);
                img.style.display = "block";
                img.style.maxWidth = "min(560px, 100%)";
                img.style.maxHeight = "420px";
                img.style.objectFit = "contain";
                img.style.margin = "12px auto";
                img.style.borderRadius = "14px";
                img.style.boxShadow = "0 6px 18px rgba(0,0,0,0.14)";
                img.style.background = "rgba(255,255,255,0.85)";

                editor.appendChild(img);
                editor.appendChild(document.createElement("br"));
            }

            lastIndex = end;
        }

        var tail = raw.substring(lastIndex);
        if (tail && tail.length > 0) {
            appendTextWithBreaks(editor, tail);
        }

        TOKEN_GLOBAL.lastIndex = 0;
    }

    function serializeEditorToTokens(editor) {
        var out = "";

        function walk(node) {
            if (node.nodeType === Node.TEXT_NODE) {
                out += node.nodeValue || "";
                return;
            }

            if (node.nodeType !== Node.ELEMENT_NODE) {
                return;
            }

            var tag = node.tagName;

            if (tag === "BR") {
                out += "\n";
                return;
            }

            if (tag === "IMG" && node.getAttribute("data-inline-img") === "1") {
                var url = node.getAttribute("data-url") || node.getAttribute("src") || "";
                if (url) {
                    out += "\n[[img:" + url + "]]\n";
                }
                return;
            }

            // 일반 요소는 자식 순회
            var children = node.childNodes;
            for (var i = 0; i < children.length; i++) {
                walk(children[i]);
            }

            // block 성격을 newline로 약간 보정(필요 최소)
            if (tag === "DIV" || tag === "P") {
                if (!out.endsWith("\n")) {
                    out += "\n";
                }
            }
        }

        var nodes = editor.childNodes;
        for (var i = 0; i < nodes.length; i++) {
            walk(nodes[i]);
        }

        // 마지막 정리
        out = out.replace(/\r/g, "");
        return out;
    }

    function insertImageAtCaret(editor, url) {
        var img = document.createElement("img");
        img.src = url;
        img.alt = "본문 이미지";
        img.setAttribute("data-inline-img", "1");
        img.setAttribute("data-url", url);
        img.style.display = "block";
        img.style.maxWidth = "min(560px, 100%)";
        img.style.maxHeight = "420px";
        img.style.objectFit = "contain";
        img.style.margin = "12px auto";
        img.style.borderRadius = "14px";
        img.style.boxShadow = "0 6px 18px rgba(0,0,0,0.14)";
        img.style.background = "rgba(255,255,255,0.85)";

        var sel = window.getSelection();
        if (!sel || sel.rangeCount === 0) {
            editor.appendChild(img);
            editor.appendChild(document.createElement("br"));
            return;
        }

        var range = sel.getRangeAt(0);

        // 커서가 editor 밖에 있으면 끝에 추가
        if (!editor.contains(range.commonAncestorContainer)) {
            editor.appendChild(img);
            editor.appendChild(document.createElement("br"));
            return;
        }

        range.deleteContents();
        range.insertNode(img);
        range.setStartAfter(img);
        range.setEndAfter(img);

        var br = document.createElement("br");
        range.insertNode(br);

        range.setStartAfter(br);
        range.setEndAfter(br);

        sel.removeAllRanges();
        sel.addRange(range);
    }

    function isEffectivelyEmpty(text) {
        var t = (text || "").replace(TOKEN_GLOBAL, "");
        TOKEN_GLOBAL.lastIndex = 0;
        t = t.replace(/\s+/g, "");
        return t.length === 0;
    }

    tools.forEach(function (tool) {
        var uploadUrl = tool.getAttribute("data-upload-url");
        var editorId = tool.getAttribute("data-target-editor");
        var textareaId = tool.getAttribute("data-target-textarea");

        var input = tool.querySelector(".inline-image-input");
        var btn = tool.querySelector(".inline-image-insert-btn");

        if (!uploadUrl || !editorId || !textareaId || !input || !btn) {
            return;
        }

        var editor = document.getElementById(editorId);
        var textarea = document.getElementById(textareaId);

        if (!editor || !textarea) {
            return;
        }

        // 초기: textarea(토큰) -> editor(이미지)
        parseTokensToEditor(editor, textarea.value || "");

        function syncToTextarea() {
            textarea.value = serializeEditorToTokens(editor);
        }

        editor.addEventListener("input", function () {
            syncToTextarea();
        });

        // 폼 submit 직전 동기화 + 빈 값 체크
        var form = editor.closest("form");
        if (form) {
            form.addEventListener("submit", function (e) {
                syncToTextarea();
                if (isEffectivelyEmpty(textarea.value)) {
                    alert("내용을 입력해 주세요.");
                    e.preventDefault();
                }
            });
        }

        btn.addEventListener("click", function () {
            input.value = "";
            input.click();
        });

        input.addEventListener("change", function () {
            var file = input.files && input.files[0];
            if (!file) {
                return;
            }
            if (!file.type || !file.type.startsWith("image/")) {
                alert("이미지 파일만 선택할 수 있습니다.");
                return;
            }

            btn.disabled = true;
            btn.textContent = "업로드 중...";

            uploadImage(uploadUrl, file)
                .then(function (url) {
                    editor.focus();
                    insertImageAtCaret(editor, url);
                    syncToTextarea();
                })
                .catch(function () {
                    alert("이미지 업로드에 실패했습니다.");
                })
                .finally(function () {
                    btn.disabled = false;
                    btn.textContent = "본문에 이미지 삽입";
                });
        });
    });
});
