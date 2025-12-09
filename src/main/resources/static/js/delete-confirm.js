document.addEventListener("DOMContentLoaded", function () {

    // 팝업 요소 찾기
    const popup = document.getElementById("delete-popup");
    const overlay = document.getElementById("delete-popup-overlay");
    const confirmBtn = document.getElementById("delete-popup-confirm");
    const cancelBtn = document.getElementById("delete-popup-cancel");

    let currentForm = null;

    // 모든 삭제 폼에 대해 이벤트 적용
    document.querySelectorAll(".delete-diary-form").forEach(form => {
        form.addEventListener("submit", function (e) {
            e.preventDefault();      // 기본 제출 막기
            currentForm = form;     // 어떤 삭제 버튼인지 기억
            openPopup();
        });
    });

    function openPopup() {
        popup.classList.add("open");
        overlay.classList.add("open");
    }

    function closePopup() {
        popup.classList.remove("open");
        overlay.classList.remove("open");
    }

    // 확인 버튼 → 실제 form 제출
    confirmBtn.addEventListener("click", function () {
        if (currentForm) currentForm.submit();
    });

    // 취소 버튼 → 닫기
    cancelBtn.addEventListener("click", function () {
        closePopup();
    });

    // 바깥 클릭 시 닫힘
    overlay.addEventListener("click", function () {
        closePopup();
    });
});
