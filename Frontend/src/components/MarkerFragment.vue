<template>
  <div id="frag" class="filter-result-fragment">
    <div id="frag-left">
      <div id="marker-name">{{ name }}</div>
      <div id="marker-address">{{ address }}</div>
      <div id="marker-tags">{{ tags }}</div>
    </div>
    <div id="frag-right">
      <div class="frag-link">
        <img alt="star" :src="starSrc">
        <span>{{ likes }}</span>
      </div>
      <div class="frag-link" @click="findKakaoRoad(latitude,longitude)">
        <img alt="kakaomap" src="../assets/kakaomap.png">
        길찾기
      </div>
      <div class="frag-link" @click="notiOn = true">
        <img alt="share" src="../assets/share.png">
        공유하기
      </div>
    </div>
    <div id="divider"/>
    <transition name="noti">
      <noti v-if="notiOn" @notiEvent="notiOn = false" :text="'기능 개발 중 입니다'"></noti>
    </transition>
  </div>
</template>

<script>
import noti from './Noti.vue'

export default {
  data() {
    return {
      notiOn: false
    }
  },
  components: {
    noti
  },
  props: ['name','address','tags','starSrc','likes','latitude','longitude'],
  methods: {
    findKakaoRoad: function(latitude, longitude) {
      window.open(`https://map.kakao.com/link/to/선택한마커,${latitude},${longitude}`)
    }
  }

}
</script>

<style>

.filter-result-fragment {
  padding: 10px 20px 10px 20px;
  display: grid;
  grid-template-columns: 2fr 1fr;
  width: inherit;
  height: fit-content;
  -webkit-transition-duration: 0.2s;
  transition-duration: 0.2s;
}
.filter-result-fragment:hover {
  background-color: rgba(223, 160, 157, 0.2);
}

#frag-left {
  text-align: left;
  display: flex;
  align-content: flex-start;
  flex-direction: column;
}

#frag-right {
  display: flex;
  justify-content: center;
  align-content: center;
  align-items: center;
  flex-direction: column;
}

#marker-name {
  font-family: Pretendard-Bold;
  font-size: 16px;
  overflow: hidden;
}

#marker-address {
  font-size: 13px;
  overflow: hidden;
}

#marker-tags {
  font-size: 11px;
  overflow: hidden;
}

.frag-link {
  display: inline-flex;
  align-items: center;
  align-content: center;
  justify-content: center;
  font-size: 11px;
}

.frag-link img {
  margin-right: 5px;
  width: 10px;
  height: 10px;
}

.frag-link:hover {
  cursor: pointer;
}

</style>

<!-- 이거 다음 할 일

6. User 리모델링. 

7. signup 리모델링. 

8. backend 띄우고 테스트

9. netilyfy 설정 -->