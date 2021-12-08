# Flare_Point

## 지도에 나오지않는 장소를 공유하자!
#### - 플레어포인트는 지도에서 안내하지 않는 장소를 마커를 통해 저장, 공유하는 플랫폼입니다.
#### - COVID-19 바이러스의 확산에 따라 사람이 적은 장소로 떠나는 아웃도어 활동(ex. 낚시, 캠핑)을 즐기는 사람들이 많아지면서, 보다 장소를 편하게 공유했으면 하는 바램에 만들었습니다.
#### - 프로젝트 진행 일정: 21-07 ~

#### 해당 레포지토리는 플레어포인트 백엔드 레포지토리입니다.
---


#### 프로젝트 소개

플레어포인트 프로젝트의 자세한 회고와 소개는 [링크](https://velog.io/@bingsu_kun/%ED%86%A0%EC%9D%B4-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-FlarePoint-%EB%A7%88%EC%BB%A4-%EA%B3%B5%EC%9C%A0-%EC%A7%80%EB%8F%84)에 적어두었습니다! 

#### 주요 개발 환경

- Spring Boot (Java11)
maven을 활용하여 API와 레포지토리에 따라 Fisher, Marker, Like로 나누어 구성했습니다.
Fisher와 Marker는 각각의 레포지토리와 Service, Controller가 존재하고, Like는 레포지토리만 존재하며 비즈니스 로직적인 부분은 Marker Service에 포함했습니다.
Spring Security와 JWT를 이용하여 인증/인가 구현했습니다

- Library: JWT, Spring Security, JPA, Guava

- DataBase: PostgreSQL

- Sever: Amazon EC2, Docker

