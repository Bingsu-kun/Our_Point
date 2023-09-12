<template>
  <div id="menu-container">
    <div id="menu-title">마커생성</div>
    <div id="maker-body">
      <div id="menu-subtitle">마커이름</div>
      <input class="marker-input" :placeholder="defaultMarkerName" :value="markerName" @input="markerName = $event.target.value">
      <div id="menu-subtitle">마커 제작자의 한마디</div>
      <textarea id="description" :value="description" @input="description = $event.target.value" @keyup="checklength" class="marker-input" placeholder="마커를 설명해 주세요 (최대 200자)" style="padding: 5px; font-family: Pretendard-Regular; height: 200px; resize: none; word-break: break-all; text-overflow: clip;"></textarea>
      <p v-if="DescOver" style="color: rgb(237,40,40)" >최대 200자 까지만 가능합니다!</p>
      <div id="menu-subtitle">태그</div>
      <input class="marker-input" placeholder="#차박#낚시#바베큐 (최대 10개)" style="font-size: 12px;" :value="tagString" @input="tagString = $event.target.value">
    </div>
    <div id="private-selector">나만보기<input type="checkbox" name="isPrivate" v-model="isPrivate"></div> <br>
    <button @click="saveMarker" class="marker-make-button">저장</button>
  </div>
</template>

<script>
import axios from 'axios'
import refresh from '../getRefreshedToken.js'

export default {
  data() {
    return {
      markerName: '',
      description: '',
      tagString: '',
      isPrivate: false,

      DescOver: false
    }
  },
  props: ['latitude', 'longitude', 'place_addr'],
  computed: {
    defaultMarkerName: function() {
      return `${sessionStorage.getItem('name')}님의 마커`
    }
  },
  methods: {
    saveEvent: function(marker) {
      this.$emit('saveEvent', marker)
    },
    // description 100자 제한. (not byte)
    checklength: function() {
      const maxlen = 200
      const desc = document.getElementById('description').value
      if (desc.length > maxlen) {
        this.DescOver = true
        document.getElementById('description').value = desc.slice(0,199)
      }
      else {
        this.DescOver = false
      }
    },
    saveMarker: async function() {

      let name = this.markerName
      const latitude = this.latitude
      const longitude = this.longitude
      const place_addr = this.place_addr
      const isPrivate = this.isPrivate
      let tagString = this.tagString
      let description = this.description

      if (name === '') {
        const userName = sessionStorage.getItem("name")
        name = `${userName}님의 마커`
        this.markerName = `${userName}님의 마커`
      }
      if (description === '') {
        description = '내용이 없습니다'
        this.description = '내용이 없습니다'
      }
      if (tagString === '') {
        const userName = sessionStorage.getItem("name")
        tagString = `#${userName}`
        this.tagString = `#${userName}`
      }
      try {
        await axios({
          method: 'POST',
          url: 'https://flarepoint.herokuapp.com/marker/create',
          headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
          data: { name: name, latitude: latitude, longitude: longitude, place_addr: place_addr, isPrivate: isPrivate, tagString: tagString, description: description },
          withCredentials: true
        }).then((res) => {
          if (res.data.success === false) {
            console.log('marker create failed.' + res.data.response)
          }
          else {
            sessionStorage.setItem("apiToken", refresh(res.headers))
            this.saveEvent(res.data.response)
          }
        })
      } catch (error) {
        console.log('bad connection.' + error)
      }
    }
  }
}
</script>

<style>

#private-selector {
  font-size: 11px;
  margin: 10px 0;
  width: 80px;
  height: 30px;
  border: 1px solid #cacaca;
  border-radius: 10px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

#maker-body {
  width: -webkit-fill-available;
  padding: 0 20px;
}

.marker-input {
  margin: 10px 0;
  width: inherit;
  height: 30px;
  border: 1px solid #cacaca;
  border-radius: 10px;
  white-space: normal;
}

.marker-input:focus {
  border: 2px solid #F3776B;
}

.marker-make-button {
  margin: 20px 0;
  width: 60%;
  height: 40px;
  border-radius: 10px;
  color: white;
  background-color: #F3776B;
  -webkit-transition-duration: 0.4s; /* Safari */
  transition-duration: 0.4s;
  border: 2px solid #F3776B;
  text-align: center;
}

.marker-make-button:hover {
  background-color: rgb(255,255,255);
  color: black;
}

</style>