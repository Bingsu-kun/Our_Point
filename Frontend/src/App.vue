<template>
  <div class="container" oncontextmenu="return false;" ondragstart="return false;" ondrop="return false;">
    <div id="menu_bar" >
      <div id="menus" class="left-menu">
        <button class="menu-buttons" @click="SHOW_FILTER = !SHOW_FILTER; SHOW_SEARCH = false; SHOW_MY = false">테마별마커</button>
        <button class="menu-buttons" @click="SHOW_SEARCH = !SHOW_SEARCH; SHOW_FILTER = false; SHOW_MY = false">위치찾기</button>
      </div>
      <div id="menus" style="justify-content: center;">
        <img class="menu_logo" alt="main_logo" title="플레어포인트" src="./assets/logo.png">
      </div>
      <div id="menus" class="right-menu">
        <button class="menu-buttons" @click="SHOW_MY = !SHOW_MY; SHOW_FILTER = false; SHOW_SEARCH = false">마이마커</button>
        <button class="menu-buttons" v-if="!LOGIN" @click="SHOW_LOGIN_FORM = true">로그인</button>
        <button class="menu-buttons" v-if="LOGIN" @click="SHOW_USER_PROFILE = true">프로필</button>
      </div>
    </div>
    <div id="map_marker_wrapper">
      <kakao-map :LOGIN="LOGIN" :SHOW_FILTER="SHOW_FILTER" :SHOW_MY="SHOW_MY" :SHOW_SEARCH="SHOW_SEARCH" @menuCloseEvent="menuCloseEvent" @showLoginForm="SHOW_LOGIN_FORM = true" @logout="setLogout"></kakao-map>
    </div>
    <transition name="fade">
      <div id="login-background" v-if="SHOW_LOGIN_FORM" @click="SHOW_LOGIN_FORM = false">
        <div id="login-foreground" v-if="SHOW_LOGIN_FORM" @click.stop>
          <login-and-signup @loginEvent="setLogin"></login-and-signup>
        </div>
      </div>
    </transition>
    <transition name="fade">
      <div id="login-background" v-if="SHOW_USER_PROFILE" @click="SHOW_USER_PROFILE = false">
        <div id="login-foreground" v-if="SHOW_USER_PROFILE" @click.stop style="width: 400px">
          <user @menuCloseEvent="SHOW_USER_PROFILE = false" @logout="setLogout"></user>
        </div>
      </div>
    </transition>
    <transition name="noti">
      <noti v-if="notiOn" @notiEvent="notiOn = false" :text="notiText"></noti>
    </transition>
    <on-boarding v-if="onBoardingOn" @onBoardingCloseEvent="onBoardingOn = false"></on-boarding>
  </div>
</template>

<script>
import LoginAndSignup from './components/LoginAndSignup.vue'
import KakaoMap from './components/Map.vue'
import User from './components/User.vue'
import refresh from './getRefreshedToken.js'
import axios from 'axios'
import Noti from './components/Noti.vue'
import OnBoarding from './components/OnBoarding.vue'
import getCookie from './getCookie.js'

export default {
  name: 'App',
  mounted() {
    if (sessionStorage.getItem('apiToken') || getCookie("refreshToken"))
      this.tokenAvailableCheck()
    this.onBoardingCheck()
  },
  data() {
    return {
      LOGIN: false,
      SHOW_LOGIN_FORM: false,
      SHOW_USER_PROFILE: false,
      SHOW_FILTER: false,
      SHOW_SEARCH: false,
      SHOW_MY: false,
      notiOn: false,
      notiText: '',
      onBoardingOn: true
    }
  },
  components: {
    LoginAndSignup,
    KakaoMap,
    User,
    Noti,
    OnBoarding
  },
  methods: {
    getLikedMarkers: async function() {
      try {
        await axios({
          method: 'GET',
          url: 'https://flarepoint.herokuapp.com/marker/mylikelist',
          headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
          withCredentials: true
        }).then((res) => {

          if (res.data.success === false) {
            console.log('get liked markers failed.')
          }
          else {
            sessionStorage.setItem('liked', JSON.stringify(res.data.response))
            sessionStorage.setItem("apiToken", refresh(res.headers))
          }
        })
      } catch (error) {
        this.setLogout('토큰이 만료되어서 ')
      }
    },
    getMyMarkers: async function() {
      try {
        await axios({
          method: 'GET',
          url: 'https://flarepoint.herokuapp.com/marker/mymarkers',
          headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
          withCredentials: true
        }).then((res) => {

          if (res.data.success === false) {
            this.setLogout('토큰이 만료되어서 ')
            console.log('get my markers failed.')
          }
          else {
            sessionStorage.setItem('my', JSON.stringify(res.data.response))
            sessionStorage.setItem("apiToken", refresh(res.headers))
          }
        })
      } catch (error) {
        this.setLogout('토큰이 만료되어서 ')
      }
    },
    tokenAvailableCheck: async function() {
      try {
        await axios({
          method: 'GET',
          url: 'https://flarepoint.herokuapp.com/fisher/me',
          headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
          withCredentials: true
        }).then((res) => {
          if (res.data.success === false) {
            this.setLogout('토큰이 만료되어서 ')
            console.log('need relogin')
          }
          else {
            sessionStorage.setItem("apiToken", refresh(res.headers))
            this.setLogin()
          }
        })
      } catch (error) {
        console.log('need relogin')
        this.setLogout('토큰이 만료되어서 ')
      }
    },
    onBoardingCheck: function() {
      if (localStorage.getItem('check') === 'false')
        this.onBoardingOn = false
    },
    setLogin: function() {
      this.LOGIN = true
      this.SHOW_LOGIN_FORM = false
      this.getLikedMarkers()
      this.getMyMarkers()
    },
    setLogout: function(cause) {
      this.notiText = `${cause}로그아웃 되었습니다`
      this.notiOn = true
      this.SHOW_USER_PROFILE = false
      sessionStorage.clear()
      this.LOGIN = false
    },
    menuCloseEvent: function() {
      this.SHOW_FILTER = false
      this.SHOW_SEARCH = false
      this.SHOW_MY = false
      this.SHOW_LOGIN_FORM = false
      this.SHOW_USER_PROFILE = false
    }
  }
}
</script>

<style>

html {
  font-size: 10px;
}

body {
  margin: 0px;
  padding: 0px;
}

button {
  font-family: Pretendard-Regular;
}

input {
  font-family: Pretendard-Regular;
  padding: 1px 5px;
}

input:focus {
  outline: none;
}

#app {
  font-family: Pretendard-Regular, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#menu_bar {
  padding: 0 20px;
  height: 60px;
  background: white;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 5;
  text-align: center;
  display: grid;
  grid-template-columns: 5fr 1fr 5fr;
}

#menus {
  display: flex;
  justify-items: center;
  align-content: center;
  align-items: center;
}

.left-menu {
  justify-content: flex-end;
}

.right-menu {
  justify-content: flex-start;
}

.menu-buttons {
  margin: 0 20px;
  padding: 0;
  height: 60px;
  border: 0;
  background-color: white;
  color: black;
  font-family: Pretendard-Bold;
  transition-duration: 0.2s;
}

.menu-buttons:hover {
  color: #F3776B
}

.menu-buttons:hover {
  cursor: pointer;
}

.menu_logo {
  height: 40px;
}

#map_marker_wrapper {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: calc(100% - 60px);
  z-index: 1;
}

#login-background {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  -webkit-box-pack: center;
  align-items: center;
  -webkit-box-align: center;
  background-color: rgba(0,0,0,0.5);
  z-index: 7;
}

#login-foreground {
  width: 300px;
  height: fit-content;
  border-radius: 20px;
  background-color: white;
  box-shadow: 0 1px 10px 1px #F3776B;
  text-align: center;
  z-index: 8;
}

.container {
  width: -webkit-fill-available;
  height: -webkit-fill-available;
  font-size: 2rem;
}

.just-fade-in-enter-active {
  -webkit-animation: fade-in 0.2s ease;
  animation: fade-in 0.2s ease;
}

.fade-enter-active {
  -webkit-animation: fade-in 0.2s ease-out;
  animation: fade-in 0.2s ease-out;
}

.fade-leave-active {
  -webkit-animation: fade-out 0.2s ease-out;
  animation: fade-out 0.2s ease-out;
}

@media screen and (max-width: 768px){
  .menu-buttons {
    font-size: 11px;
    margin: 0 15px;
  }
  .left-menu {
    justify-content: space-around;
  }
  .right-menu {
    justify-content: space-around;
  }
}
@media screen and (max-width: 400px){
  .menu-buttons {
    font-size: 9px;
    margin: 0 10px;
  }
}

@-webkit-keyframes fade-in {
  0% {
            opacity: 0;
  }
  100% {
            opacity: 1;
  }
}
@keyframes fade-in {
  0% {
            opacity: 0;
  }
  100% {
            opacity: 1;
  }
}
@-webkit-keyframes fade-out {
  0% {
            opacity: 1;
  }
  100% {
            opacity: 0;
  }
}
@keyframes fade-out {
  0% {
            opacity: 1;
  }
  100% {
            opacity: 0;
  }
}
@-webkit-keyframes make-in {
  0% {
    width: 0;
    height: 0;
  }
  100% {
    width: 150px;
    height: 25px;
  }
}
@keyframes make-in {
  0% {
    width: 0;
    height: 0;
  }
  100% {
    width: 150px;
    height: 25px;
  }
}

@font-face {
    font-family: 'Pretendard-Regular';
    src: url('https://cdn.jsdelivr.net/gh/Project-Noonnu/noonfonts_2107@1.1/Pretendard-Regular.woff') format('woff');
    font-weight: 400;
    font-style: normal;
}
@font-face {
    font-family: 'Pretendard-Bold';
    src: url('https://cdn.jsdelivr.net/gh/Project-Noonnu/noonfonts_2107@1.1/Pretendard-Bold.woff') format('woff');
    font-weight: 700;
    font-style: normal;
}
@font-face {
    font-family: 'Pretendard-Black';
    src: url('https://cdn.jsdelivr.net/gh/Project-Noonnu/noonfonts_2107@1.1/Pretendard-Black.woff') format('woff');
    font-weight: 900;
    font-style: normal;
}

</style>
