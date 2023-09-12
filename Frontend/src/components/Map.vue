<template>
  <div id="map" @contextmenu="initMarkerButtonListener">
    <div id="loading-text-container" v-if="isLoading">
      <div id="loading-text">
        {{loadingText}}
      </div>
    </div>
    <loading :active="isLoading" :can-cancel="true" :color="loading_color" :is-full-page="fullscreen"/>
    <div id="menu-background" v-if="SHOW_SAVE_MAKER" @click="SHOW_SAVE_MAKER = !SHOW_SAVE_MAKER"/>
    <transition name="menu">
      <div class="menu-foreground" v-if="SHOW_SAVE_MAKER">
        <save-maker :latitude="centerLat" :longitude="centerLng" :place_addr="place_addr" @saveEvent="saveEvent"></save-maker>
      </div>
    </transition>
    <transition name="menu">
      <div class="menu-foreground" v-if="SHOW_UPDATE_MAKER">
        <update-maker :selected="selected" @updateEvent="updateEvent"></update-maker>
      </div>
    </transition>
    <transition name="menu">
      <div class="menu-foreground" v-if="SHOW_FILTER">
        <menu-filter :markers="markers" :allMarkersLikes="allMarkersLikes" @disableMarkers="disableMarkers" @selectedEvent="selectedEvent" @menuCloseEvent="enableMarkers(menuCloseEvent)"></menu-filter>
      </div>
    </transition>
    <transition name="menu">
      <div class="menu-foreground" v-if="SHOW_SEARCH">
        <menu-search @selectedEvent="selectedEvent" @menuCloseEvent="menuCloseEvent"></menu-search>
      </div>
    </transition>
    <transition name="menu">
      <div class="menu-foreground" v-if="SHOW_MY">
        <menu-my :allMarkersLikes="allMarkersLikes" @logout="logout" @selectedEvent="selectedEvent" @menuCloseEvent="menuCloseEvent"></menu-my>
      </div>
    </transition>
    <transition name="menu">
      <div class="menu-foreground" v-if="SHOW_THIS_MARKER">
        <marker-overlay :LOGIN="LOGIN" :selected="selected" @likeUpdate="getAllMarkersLikes" @doUpdate="editEvent" @deleteEvent="deleteEvent" @showLoginForm="showLoginForm" @overlayCloseEvent="SHOW_THIS_MARKER = false"></marker-overlay>
      </div>
    </transition>
  </div>
</template>

<script>
import Loading from 'vue-loading-overlay';
import 'vue-loading-overlay/dist/vue-loading.css';
import axios from 'axios';

import SaveMaker from './SaveMaker.vue'
import UpdateMaker from './UpdateMaker.vue'
import MenuFilter from './Filter.vue'
import MenuSearch from './Search.vue'
import MenuMy from './MyMarker.vue'
import MarkerOverlay from './MarkerOverlay.vue'

export default {

  name: 'Map',
  mounted() {
    if (window.kakao && window.kakao.maps) {
      try {
        this.initKakaoMap();
        setTimeout(() => {
          loadMarker(() => {
            this.isLoading = false
          })
        }, 6000)
      } catch(e) {
        console.log("Fail to load Kakao map." + e)
      }
    }
    else {
      try {
        document.getElementById('kakaoScript').onload = () => kakao.maps.load(this.initKakaoMap);
        setTimeout(() => {
          loadMarker(() => {
            this.isLoading = false
          })
        }, 6000)
      } catch(e) {
        console.warn("Fail to load Kakao map." + e)
      }
    }
    const loadMarker = async (callback) => {
      this.loadingText = '마커를 불러오는 중...'
      await this.getAllMarkers()
      await this.getAllMarkersLikes()
      callback()
    }
  },
  data() {
    return {
      map: null,
      markers: [],
      renderedMarkers: [],
      allMarkersLikes: [],
      selected: null,
      
      loadingText: '테스트 텍스트',
      isLoading: false,
      fullscreen: false,
      loading_color: "#E2004B",

      centerLat: '',
      centerLng: '',
      place_addr: '',
      
      SHOW_SAVE_MAKER: false,
      SHOW_THIS_MARKER: false,
      SHOW_UPDATE_MAKER: false,

      SidebarButtonSrc: require("../assets/sidebar.png")
    }
  },
  props: ['LOGIN','SHOW_FILTER','SHOW_SEARCH','SHOW_MY'],
  components: {
    Loading,
    SaveMaker,
    UpdateMaker,
    MenuFilter,
    MenuSearch,
    MenuMy,
    MarkerOverlay
  },
  methods: {
    // 카카오 맵 초기화 메서드
    initKakaoMap: function() {
      this.isLoading = true
      this.loadingText = '맵을 불러오는 중...'
      const mapContainer = document.querySelector("#map");
      navigator.geolocation.getCurrentPosition((position) => {
        this.lat = position.coords.latitude
        this.lng = position.coords.longitude
        this.map = init(this.lat,this.lng,mapContainer)
      },() => {
        console.log("현재 위치를 파악할 수 없습니다.")
        //강남역
        this.lat = 37.49780947181307
        this.lng = 127.02766764268932
        this.map = init(this.lat,this.lng,mapContainer)
      })
  
      function init(lat,lng,mapContainer) {
        const options = {
          center: new kakao.maps.LatLng(lat,lng),
          level: 3
        };
        const map = new kakao.maps.Map(mapContainer, options);
        // 지도 생성
        map.setMapTypeId(kakao.maps.MapTypeId.ROADMAP);

        // 지도 타입 변경 컨트롤을 생성한다
        const mapTypeControl = new kakao.maps.MapTypeControl();

        // 지도의 상단 우측에 지도 타입 변경 컨트롤을 추가한다
        map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

        // 지도 클릭 이벤트를 등록한다 (좌클릭 : click, 우클릭 : rightclick, 더블클릭 : dblclick)
        // 맵 위 어디든지 좌클릭 시 이미 있는 마커메이커를 없앤다.
        kakao.maps.event.addListener(map, 'click', function (mouseEvent) {
          const maker = document.querySelector('#marker-maker')
          if (maker !== null)
            maker.remove()
        });

        // 지도 우클릭 이벤트를 등록한다
        // 맵 위 어디든지 우클릭 시 마커메이커 오버레이 창을 띄운다. 이미 마커메이커가 띄워져있는 경우, 기존 메이커를 지우고 만든다.
        kakao.maps.event.addListener(map, 'rightclick', function(mouseEvent) {
          let beforeMaker = document.querySelector('#marker-maker')
          map.panTo(new kakao.maps.LatLng(mouseEvent.latLng.Ma, mouseEvent.latLng.La))
          sessionStorage.setItem('rightLat',mouseEvent.latLng.Ma)
          sessionStorage.setItem('rightLng',mouseEvent.latLng.La)
          if (beforeMaker === null) {
            const markermaker = new kakao.maps.CustomOverlay({
              map: map,
              clickable: true, //커스텀 오버레이 클릭시 지도에 이벤트 전파 방지
              content: '<div id="marker-maker"><button class="marker-button">여기에 마커 만들기</button></div>',
              position: new kakao.maps.LatLng(mouseEvent.latLng.Ma,mouseEvent.latLng.La),
              xAnchor: 0,
              yAnchor: 0
            })
          }
          else {
            beforeMaker.remove()
            beforeMaker = new kakao.maps.CustomOverlay({
              map: map,
              clickable: true, //커스텀 오버레이 클릭시 지도에 이벤트 전파 방지
              content: '<div id="marker-maker"><button class="marker-button">여기에 마커 만들기</button></div>',
              position: new kakao.maps.LatLng(mouseEvent.latLng.Ma,mouseEvent.latLng.La),
              xAnchor: 0,
              yAnchor: 0
            })
          }
        })
        
        return map;
      }
    },
    initMarkerButtonListener: function() {
      const button = document.querySelector('.marker-button')
      const maker = document.querySelector('#marker-maker')
      const geocoder = new kakao.maps.services.Geocoder();
      this.centerLat = sessionStorage.getItem('rightLat')
      this.centerLng = sessionStorage.getItem('rightLng')
      const coord = new kakao.maps.LatLng(this.centerLat, this.centerLng);
      const callback = (result, status) => {
        if (status === kakao.maps.services.Status.OK) {
          this.place_addr = result[0].address.address_name
        }
      }
      geocoder.coord2Address(coord.getLng(), coord.getLat(), callback);
      setTimeout(() => {
        button.addEventListener("click",() => {
          maker.remove()
          if (this.LOGIN === false) {
            this.showLoginForm()
          }
          else {
            this.menuCloseEvent()
            this.SHOW_SAVE_MAKER = true
            
            this.map.setLevel(1,{ animate: true })
          }
        })
      },100)
    },
    //모든 마커들을 서버에서 가져온다.
    getAllMarkers: async function() {
      this.markers = []
      try {
        //axios로 모든 마커 가져오기
        await axios({
          method: 'GET',
          url: 'https://flarepoint.herokuapp.com/marker/all',
          withCredentials: true
        }).then((res) => {

          if (res.data.success === false) {
            console.log('marker loading failed by server issue.')
          }
          else {
            this.markers = res.data.response
          }

        })
      } catch (error) {
        console.log('marker loading failed.' + error)
      }
    },
    getAllMarkersLikes: async function() {
      let markerIds = []
      for (let i = 0; i < this.markers.length; i++) {
        markerIds.push(this.markers[i].markerId)
      }
      this.allMarkersLikes = []
      try {
        //axios로 모든 마커 가져오기
        await axios({
          method: 'POST',
          url: 'https://flarepoint.herokuapp.com/marker/likes',
          data: { markerIds: markerIds },
          withCredentials: true
        }).then((res) => {

          if (res.data.success === false) {
            console.log('marker loading failed by server issue.')
          }
          else {
            for (let i = 0; i < markerIds.length; i++) {
              this.allMarkersLikes.push([markerIds[i],res.data.response[i]])
            }
          }

        })
      } catch (error) {
        console.log('marker loading failed.' + error)
      } finally {
        if (this.markers !== []) {
          for (let i = 0; i < this.markers.length; i++){
                this.renderMarker(this.markers[i],this.allMarkersLikes[i].like)
          }
        }
      }
    },
    renderMarker: function(mk,likes) {
      const Lat = mk.latitude
      const Lng = mk.longitude
      const markerImageUrl = (likes) => {
        if (likes >= 100){
          return ''// 100 이상일 경우 마커 색상
        }
        else if (likes >= 50) {
          return ''// 50
        }
        else if (likes >= 20){
          return ''// 20 이상일 경우 마커 색상
        }
        else {
          return require('../assets/normal-marker.png')
        }
      } 
      if (mk.isPrivate === false) {
        // 지도에 마커를 생성하고 표시한다
        const marker = new kakao.maps.Marker({
          position: new kakao.maps.LatLng(Lat,Lng), // 마커의 좌표
          image : new kakao.maps.MarkerImage(markerImageUrl(likes), new kakao.maps.Size(36, 36), { offset : new kakao.maps.Point(18, 36) }), // 마커의 이미지
          map: this.map, // 마커를 표시할 지도 객체
          title: mk.markerId
        });

        kakao.maps.event.addListener(marker,'click',() => {
          this.map.panTo(new kakao.maps.LatLng(Lat,Lng))
          this.selected = findSelected(this.markers,Lat,Lng)
          this.SHOW_THIS_MARKER = true
        })
    
        this.renderedMarkers.push(marker)
      }
      else {
        //isPrivate가 true일 경우 제작자가 지금 로그인한 사람과 같은지 확인 후 렌더한다.
        const id = sessionStorage.getItem('id')
        if (parseInt(id) === mk.fisherId ){
          const marker = new kakao.maps.Marker({
            position: new kakao.maps.LatLng(Lat,Lng), // 마커의 좌표
            image : new kakao.maps.MarkerImage(markerImageUrl(likes), new kakao.maps.Size(36, 36), { offset : new kakao.maps.Point(18, 36) }), // 마커의 이미지
            map: this.map, // 마커를 표시할 지도 객체
            title: mk.markerId
          });

          kakao.maps.event.addListener(marker,'click',() => {
            this.map.panTo(new kakao.maps.LatLng(Lat,Lng))
            this.selected = findSelected(this.markers,Lat,Lng)
            this.SHOW_THIS_MARKER = true
          })

          this.renderedMarkers.push(marker)
        }
      }
      function findSelected (markers,Lat,Lng) {
        for (let mk of markers) {
          if (mk.latitude === Lat && mk.longitude === Lng)
            return mk
        }
      }
    },
    saveEvent: function(savedMarker) {
      this.SHOW_SAVE_MAKER = false
      this.renderMarker(savedMarker)

      //새로 생성된 마커를 배열에 추가
      this.markers.push(savedMarker)
      const my = JSON.parse(sessionStorage.getItem('my'))
      my.push(savedMarker)
      sessionStorage.setItem('my',JSON.stringify(my))
      this.map.setLevel(3,{ animate: true })
    },
    updateEvent: function(updatedMarker) {
      this.selected = updatedMarker
      this.SHOW_UPDATE_MAKER = false
      this.SHOW_THIS_MARKER = true
    },
    editEvent: function() {
      this.SHOW_THIS_MARKER = false
      this.SHOW_UPDATE_MAKER = true
    },
    deleteEvent: function(selected) {
      this.renderedMarkers.forEach((element) => {
        if (selected.markerId === parseInt(element.getTitle()))
          element.setMap(false)
      })
      for (let i = 0; i < this.markers.length; i++) {
        if (this.markers[i].markerId === selected.markerId)
          this.markers.splice(i,0)
          break
      }
    },
    selectedEvent: function(Lat,Lng) {
      this.map.panTo(new kakao.maps.LatLng(Lat,Lng))
    },
    menuCloseEvent: function() {
      this.$emit("menuCloseEvent")
    },
    disableMarkers: function(markers) {
      this.renderedMarkers.forEach((element) => {
        if (markers.indexOf(parseInt(element.getTitle())) !== -1)
          element.setVisible(false)
        else 
          element.setVisible(true)
      })
    },
    enableMarkers: function(callback) {
      this.renderedMarkers.forEach((element) => {
        if (element.getVisible() === false)
          element.setVisible(true)
      })
      callback()
    },
    showLoginForm: function() {
      this.$emit("showLoginForm")
    },
    logout: function(cause) {
      this.$emit('logout',cause)
    }
  }
}
</script>

<style>

#map {
  width: 100%;
  height: 100%;
  z-index: 2;
  align-items: center;
  align-content: center;
  justify-content: center;
}

#loading-text-container {
  display: flex;
  bottom: 0;
  left: 0;
  position: absolute;
  right: 0;
  top: 0;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  z-index: 9999;
}

#loading-text {
  display: flex;
  height: 130px;
  font-family: 'Pretendard-Black';
  z-index: 9999;
  color:#F3776B;
  text-align: center;
  align-items: flex-end;
}

#marker-maker {
  padding: 2px 5px 5px 5px;
  width: 150px;
  height: 25px;
  background-color: white;
  border-radius: 0 20px 20px 20px;
  border: 2px solid rgb(150,150,150);
  z-index: 3;
  -webkit-animation: make-in 0.2s ease-out;
  animation: make-in 0.2s ease-out;
}

#maker-button-list {
  width: 100%;
  height: fit-content;
}

#menu-background {
  width: 100%;
  height: 100%;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 3;
}

#menu-container {
  height: 100%;
  padding: 20px 0;
}

.menu-foreground {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 320px;
  height: 100%;
  border-radius: 0 20px 20px 0;
  background-color: white;
  box-shadow: 1px 0 20px 5px #F3776B;
  text-align: center;
  justify-content: center;
  z-index: 6;
  overflow: hidden;
}

.marker-button {
  width: 100%;
  height: 25px;
  border-radius: 20px;
  color: black;
  background-color: white;
  border: 2px solid #F3776B;
  transition-duration: 0.2s;
  overflow: hidden;
}

.marker-button:hover {
  background-color: #F3776B;
  color: white;
}

.make-marker-enter-active {
  -webkit-animation: make-in 0.2s ease-out;
  animation: make-in 0.2s ease-out;
}

.menu-enter-active {
  -webkit-animation: menu-in 0.3s ease-out;
  animation: menu-in 0.3s ease-out;
}
.menu-leave-active {
  -webkit-animation: menu-out 0.3s ease-in;
  animation: menu-out 0.3s ease-in;
}

@-webkit-keyframes menu-in {
  0% {
    -webkit-transform: translateX(-100%);
            transform: translateX(-100%);
  }
  100% {
    -webkit-transform: translateX(0%);
            transform: translateX(0%);
  }
}
@keyframes menu-in {
  0% {
    -webkit-transform: translateX(-100%);
            transform: translateX(-100%);
  }
  100% {
    -webkit-transform: translateX(0%);
            transform: translateX(0%);
  }
}
@-webkit-keyframes menu-out {
  0% {
    -webkit-transform: translateX(0%);
            transform: translateX(0%);
  }
  100% {
    -webkit-transform: translateX(-100%);
            transform: translateX(-100%);
  }
}
@keyframes menu-out {
  0% {
    -webkit-transform: translateX(0%);
            transform: translateX(0%);
  }
  100% {
    -webkit-transform: translateX(-100%);
            transform: translateX(-100%);
  }
}
</style>