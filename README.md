# 2026-usw-festival-web-backend

- 관리자 계정은 외부에서 미리 생성해 DB에 저장한다.
- 관리자 비밀번호는 BCrypt 해시로 저장한다.
- 다른 오리진 프론트 연동 시 `ADMIN_ALLOWED_ORIGINS` 환경 변수로 허용 Origin을 설정한다.
- 기본 허용 Origin은 `http://localhost:3000`, `http://127.0.0.1:3000`, `http://localhost:5173`, `http://127.0.0.1:5173` 이다.
- 운영/스테이징에서는 `ADMIN_ALLOWED_ORIGINS=https://admin.example.com,https://staging-admin.example.com` 형태로 덮어쓴다.
