<template>
  <div id="menu-container">
    <loading :active="isLoading" :can-cancel="true" :color="loading_color" :is-full-page="fullscreen"/>
    <button class="close" @click="menuCloseEvent"></button>
    <div id="menu-title">위치 찾기</div>
    <div id="search-form">
      <input class="search-input" :value="keyword" @input="keyword = $event.target.value" placeholder="장소, 주소, 지명 검색" @keydown.enter="search">
      <button class="search-button" @click="search"></button>
    </div>
    <div id="search-result" v-if="!noResult">
      <div @click="selectedEvent(result.y,result.x)" id="frag" class="search-result-fragment" v-for="result in searchResults" :key="result.id" >
        <div id="place-name">{{ result.place_name }}</div>
        <div id="place-address">
          <div>{{ result.road_address_name }}</div>
          <div>(지번) {{ result.address_name }}</div>
        </div>
        <div id="place-phone">{{ result.phone }}</div>
        <div id="divider"/>
      </div>
    </div>
    <div id="no-search-result" v-if="noResult">
      <img alt="no result" src="../assets/not_found.png">
    </div>
  </div>
</template>

<script>
import Loading from 'vue-loading-overlay';
import 'vue-loading-overlay/dist/vue-loading.css';

export default {
  data() {
    return {
      keyword: '',
      searchResults: [],
      noResult: false,

      isLoading: false,
      fullscreen: false,
      loading_color: "#E2004B",
    }
  },
  components: {
    Loading
  },
  methods: {
    search: function() {
      this.isLoading = true
      const searchResult = new kakao.maps.services.Places()
      const keyword = this.keyword
      this.searchResults = []
      let result = this.searchResults
      if (keyword !== '' && keyword.length > 1){
        searchResult.keywordSearch( keyword, placeSearched )
      }
      setTimeout(() => {
        this.isLoading = false
        this.keyword = ''
        if (this.searchResults.length === 0)
          this.noResult = true
      },2000)
      
      function placeSearched(data, status, pagination) {
        if (status === kakao.maps.services.Status.OK) {

          for ( let i = 0; i < data.length; i++ ) {
            result.push({ id: data[i].id, x: data[i].x, y: data[i].y, place_name: data[i].place_name, road_address_name: data[i].road_address_name, address_name: data[i].address_name, phone: data[i].phone })
          }
          setTimeout(() => {
            observeChild(io,pagination)
          },1000)
          
          const observerOption = {
            threshold: 0.5
          }

          const io = new IntersectionObserver((entries, observer) => {
            entries.forEach(entry => {
              if (entry.isIntersecting) {
                observer.unobserve(entry.target)
                if (pagination.hasNextPage){
                  pagination.nextPage()
                }
              }
            })
          }, observerOption)

        } else if (status === kakao.maps.services.Status.ERROR) {

          alert('검색 중 오류가 발생했습니다.');

        }
      }

      //관측 요소 갱신 함수
      function observeChild(intersectionObserver,pagination) {
        const resultChilds = document.querySelectorAll("#frag")
        resultChilds.forEach((el) => {
          if (!el.nextElementSibling) {
            intersectionObserver.observe(el)
          } else {
            intersectionObserver.disconnect()
          }
        })
      }
    },
    selectedEvent: function(Lat,Lng) {
      this.$emit("selectedEvent",Lat,Lng)
    },
    menuCloseEvent: function() {
      this.$emit("menuCloseEvent")
    }
  }

}
</script>

<style>

#menu-title {
  font-family: Pretendard-Black;
  padding: 10px 0;
  width: inherit;
  height: fit-content;
}
#menu-subtitle {
  font-family: Pretendard-Bold;
  font-size: 13px;
  text-align: left;
  padding: 10px 0;
  width: inherit;
  height: fit-content;
}

.close {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 20px;
  height: 20px;
  border: none;
  background-color: transparent;
  background-image: url("../assets/close.png");
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
  transition-duration: 0.3s;
}

.close:hover {
  width: 25px;
  height: 25px;
}

#search-form {
  margin: 20px;
  padding: 0 10px 0 15px ;
  border: 1px solid #CACACA;
  border-radius: 10px;
  display: flex;
  align-items: center;
}

#search-result {
  box-sizing: border-box;
  height: 480px;
  text-align: left;
  overflow: overlay;
  overflow-x: hidden;
}

#no-search-result {
  width: inherit;
  height: 480px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  justify-items: center;
  align-content: center;
  align-items: center;
}

#no-search-result img {
  width: 50%;
}

#place-name {
  margin: 3px 0;
  font-size: 16px;
  font-family: Pretendard-Bold;
}

#place-address {
  margin: 2px 0;
  font-size: 13px;
}
#place-phone {
  margin: 2px 0;
  margin-bottom: 10px;
  font-size: 11px;
}
#divider {
  position: relative;
  left: 0;
  width: -webkit-fill-available;
  height: 0.2px;
  background-color: #CACACA;
}

.search-input {
  width: -webkit-fill-available;
  height: 40px;
  border: 0;
}
.search-input:focus {
  font-family: Pretendard-Bold;
}

.search-button {
  margin: 5px;
  width: 15px;
  height: 15px;
  border: 0;
  border-radius: 100%;
  background-color: transparent;
  background-image: url("../assets/search.png");
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
  -webkit-transition-duration: 0.2s;
  transition-duration: 0.2s;
}
.search-button:hover {
  background-color: rgb(170, 170, 170);
  cursor: pointer;
}
.search-button:focus {
  border: 0
}

.search-result-fragment {
  padding: 10px 20px 0 20px;
  width: inherit;
  height: fit-content;
  -webkit-transition-duration: 0.2s;
  transition-duration: 0.2s;
}
.search-result-fragment:hover {
  background-color: rgba(223, 160, 157, 0.2);
}
</style>