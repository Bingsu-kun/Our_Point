<template>
  <div id="liked-markers-container">
    <div v-if="LOGIN" style="margin-top: 20px; padding: 0 10px; display:flex; justify-content: space-around;" >
      <button class="my-marker-nav" @click="isLikedMenu = true">
        <img alt="liked" :src="starOn">
      </button>
      <button class="my-marker-nav" @click="isLikedMenu = false">
        <img alt="hammer" :src="hammer">
      </button>
    </div>
    <button class="close" @click="menuCloseEvent"></button>
    <div v-if="LOGIN" style="z-index: 2">
      <div id="search-result" v-if="isLikedMenu && !LikedNoResult">
        <div id="menu-title" style="text-align: center;">좋아요 한 마커</div>
        <fragment @click="selectedEvent(likeResult.latitude,likeResult.longitude)" v-for="likeResult in likedMarkers" 
        :key="likeResult.markerId" :name="likeResult.name" :address="likeResult.place_addr" :tags="likeResult.tags"
        :starSrc="starOn" :likes="getLikes(likeResult.markerId)" :latitude="likeResult.latitude" :longitude="likeResult.longitude">
        </fragment>
      </div>
      <div id="no-search-result" v-if="isLikedMenu && LikedNoResult">
        <img alt="no result" src="../assets/no_markers.png">
      </div>
      <div id="search-result" v-if="!isLikedMenu && !MyNoResult">
        <div id="menu-title" style="text-align: center;">내가 만든 마커</div>
        <fragment @click="selectedEvent(myResult.latitude,myResult.longitude)" v-for="myResult in myMarkers" 
        :key="myResult.markerId" :name="myResult.name" :address="myResult.place_addr" :tags="myResult.tags"
        :starSrc="isLiked(myResult.markerId)" :likes="getLikes(myResult.markerId)" :latitude="myResult.latitude" :longitude="myResult.longitude">
        </fragment>
      </div>
      <div id="no-search-result" v-if="!isLikedMenu && MyNoResult">
        <img alt="no result" src="../assets/no_markers.png">
      </div>
    </div>
    <div id="no-search-result" v-if="!LOGIN">
      <div id="menu-title">
        로그인이<br>필요합니다
      </div>
    </div>
  </div>
</template>

<script>
import Fragment from './MarkerFragment.vue'

export default {
  mounted() {
    if(sessionStorage.getItem('apiToken')) {
      this.LOGIN = true
      this.likedMarkers = JSON.parse(sessionStorage.getItem('liked'))
      this.myMarkers = JSON.parse(sessionStorage.getItem('my'))
    }
  },
  components: {
    Fragment
  },
  data() {
    return {
      LOGIN: false,
      likedMarkers: [],
      likedMarkerIds: [],
      myMarkers: [],
      isLikedMenu: true,

      starOn: require("../assets/star_on.png"),
      starOff: require("../assets/star_off.png"),
      hammer: require("../assets/hammer.png")
    }
  },
  computed: {
    LikedNoResult: function() {
      return this.likedMarkers.length === 0 ? true : false
    },
    MyNoResult: function() {
      return this.myMarkers.length === 0 ? true : false
    }
  },
  props: ['allMarkersLikes'],
  methods: {
    getLikes: function(markerId) {
      for (let mk of this.allMarkersLikes) {
        if (mk[0] === markerId)
          return mk[1]
      }
    },
    isLiked: function(markerId) {
      if (this.likedMarkerIds.includes(markerId))
        return this.starOn
      else 
        return this.starOff
    },
    getLikedMarkerIds: function() {
      JSON.parse(sessionStorage.getItem("liked")).forEach((element) => {
        this.likedMarkerIds.push(element.markerId)
      })
    },
    selectedEvent(Lat, Lng) {
        this.$emit('selectedEvent',Lat,Lng)
    },
    menuCloseEvent: function() {
      this.$emit("menuCloseEvent")
    }
  } 
}
</script>
<style>
#liked-markers-container {
  padding: 20px;
}

.my-marker-nav {
  width: 120px;
  height: 30px;
  border: 0;
  border-radius: 10px 10px 0 0;
  box-shadow: 0 -5px 5px 1px #cacaca;
  background-color: white;
  transition-duration: 0.3s;
}
.my-marker-nav img {
  height: 20px;
}
.my-marker-nav:hover {
  cursor: pointer;
  box-shadow: 0;
}
</style>