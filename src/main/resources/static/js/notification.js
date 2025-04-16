<script>
    // 실시간 미확인 알림 수
    async function fetchUnreadCount() {
        try {
            const response = await fetch("/notifications/unreadCount");
            const count = await response.json();
            const badge = document.getElementById("notification-count");
            badge.textContent = count;
            badge.style.display = count > 0 ? 'inline-block' : 'none';
        } catch (err) {
            console.error("알림 수 조회 실패", err);
        }
    }

    // 알림 모달 안 내용
    async function fetchNotifications() {
        try {
            const response = await fetch("/notifications?filter=unread");
            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, "text/html");
            const innerList = doc.querySelector("#notificationTableContainer")?.innerHTML || "<p>알림이 없습니다.</p>";
            document.getElementById("notification-list").innerHTML = innerList;
        } catch (err) {
            console.error("알림 목록 가져오기 실패", err);
        }
    }

    // 모달 열릴 때마다 알림 목록 다시 불러오기
    document.getElementById("notificationIcon").addEventListener("click", fetchNotifications);

    // 주기적으로 갱신
    setInterval(fetchUnreadCount, 30000); // 30초마다 확인
    window.addEventListener("DOMContentLoaded", fetchUnreadCount); // 첫 로딩 시도
</script>
