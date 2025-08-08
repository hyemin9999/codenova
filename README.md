git push 방법

1. git branch로 현재 f/user인지 확인할 것
2. git status / git add .
3. git commit  -m "설명"
4. 마지막에 git push origin f/user 로 쓸것


현재 구현 사항 8.7(목)

로그인 / 회원가입 로직 구현함 + 임시용 메인화면 및 nav바 적용시킴
비밀번호 특수문자/대문자 조건 넣음
아이디 / 이메일 중복확인 테스트바람


이메일 유효성 검사 테스트가 좀더 필요함

현재 구현 사항 8.8(금)

아이디 찾기 구현 / 로직은 아직 더 공부 필요(이해하는 과정)
아이디 찾기 findid중 BindingResult는 find...java 같은 클래스 내부의 모든 객체를 검사함으로
           email만 골라내기 위해 따로 클래스 UserFindIdForm 신설해서 따로 넣어둠
