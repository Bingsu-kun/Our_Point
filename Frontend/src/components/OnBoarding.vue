<template>
  <div id="on-boarding-background" style="display: flex; flex-direction: column; justify-content: center; align-items: center;">
    <div id="on-boarding-menu-desc">
      <p>테마별마커<br><br>원하는 마커만 검색할 수 있습니다!<br>인기 태그를 통한 빠른 검색도 가능합니다.<br>검색어를 넣지 않고 검색하면 모든 마커들을 목록으로 보여줍니다.<br>목록에서 원하는 마커를 클릭하면 해당 마커로 지도가 옮겨집니다.</p>
      <p>위치찾기<br><br>원하는 지역을 검색할 수 있습니다!<br>카카오API를 이용한 지도 검색 기능입니다.<br>검색 후 원하는 지역을 목록에서 클릭하면 해당 지역으로 지도를 옮깁니다.</p>
      <p>마이마커<br><br>별표해 두었던 마커나 내가 만든 마커를 담은 목록입니다.<br>목록에서 원하는 마커를 클릭하면 해당 마커로 지도가 옮겨집니다.</p>
    </div>
    <div id="on-boarding-marker-desc">
      <p>로그인을 하고, 지도 위에서 우클릭 하면 새로운 마커를 만들 수 있습니다!</p>
      <img src="../assets/normal-marker.png" alt="marker" style="width: 20%;">
      <p>지도 위 마커를 클릭하면 해당 마커의 세부정보를 볼 수 있습니다!</p>
    </div>
    <p style="position: fixed; right: 40px; top: 10px; width: 100px"><input @click="input = !input" type="checkbox" name="onBoardingDismiss" value="false">다시 보지 않기</p>
    <button class="close" @click="onBoardCloseEvent"></button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      input: true
    }
  },
  methods: {
    onBoardCloseEvent: function() {
      if (this.input) {
        localStorage.setItem('check','true')
        this.$emit('onBoardingCloseEvent')
      }
      else {
        localStorage.setItem('check','false')
        this.$emit('onBoardingCloseEvent')
      }
    }
  }
}
</script>

<style>

#on-boarding-background{
  position: fixed;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: black;
  opacity: 0.5;
  z-index: 9999;
}

#on-boarding-menu-desc {
  margin: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

#on-boarding-marker-desc {
  margin: 40px;
  max-width: 800px;
  width: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

#on-boarding-background p {
  color: white;
  font-family: HSYuji-Regular;
  width: 250px;
  font-size: 16px;
}
@media screen and (max-width: 768px) {
  #on-boarding-background p {
    font-size: 13px;
  }
  #on-boarding-menu-desc {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
  }

}

@font-face {
    font-family: 'HSYuji-Regular';
    src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/noonfonts_2108@1.1/HSYuji-Regular.woff') format('woff');
    font-weight: normal;
    font-style: normal;
}

</style>