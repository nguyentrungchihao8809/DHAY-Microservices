# QUY ĐỊNH GHÉP CHUYẾN THÔNG MINH (SOP) - HỆ THỐNG DHAY

## 1. Quy tắc về Khoảng cách và Lộ trình (Spatial Rules)
- **Quãng đường lệch (Detour Distance):** Tổng quãng đường tài xế phải đi thêm để đón và trả khách ghép không được vượt quá 25% tổng quãng đường của hành trình gốc.
- **Giới hạn tuyệt đối:** Không ghép chuyến nếu quãng đường lệch vượt quá 5km, bất kể tỉ lệ phần trăm.
- **Bán kính quét (Search Radius):** Ưu tiên tìm khách hàng trong phạm vi hành lang (buffer) 1km dọc theo tuyến đường tài xế đang đi.

## 2. Quy tắc về Thời gian (Temporal Rules)
- **Thời gian chờ của khách (Waiting Time):** Khách hàng không nên chờ quá 10 phút kể từ lúc gửi yêu cầu đến lúc tài xế tới điểm đón.
- **Thời gian trễ của tài xế:** Việc ghép thêm khách không được làm tài xế về đích muộn hơn 15 phút so với dự kiến ban đầu.

## 3. Chính sách Ưu tiên (Priority Policies)
- **Hạng thành viên:** Ưu tiên ghép cặp cho khách hàng hạng Diamond và Gold trước các hạng khác.
- **An toàn ban đêm:** Từ 22h đêm đến 5h sáng, ưu tiên ghép các khách hàng nữ đi cùng nhau hoặc ưu tiên các tài xế có điểm đánh giá an toàn > 4.9.
- **Khuyến khích tài xế mới:** Tài xế có dưới 50 chuyến đi đầu tiên sẽ được ưu tiên gợi ý các cuốc ghép "dễ" (lệch ít < 500m) để làm quen hệ thống.

## 4. Bối cảnh Khu vực (Geographic Metadata)
- **Khu vực Quận 1, TP.HCM:** Thường xuyên kẹt xe từ 16h30 đến 19h00. Agent cần tự động cộng thêm 10 phút vào ETA khi tính toán qua khu vực này.
- **Khu vực Sân bay:** Ưu tiên các xe có cốp rộng (available_seats >= 3) nếu khách hàng có ghi chú về hành lý.