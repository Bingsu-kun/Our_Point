<template>
  <div id="menu-container">
    <div id="menu-title">마커생성</div>
    <div id="maker-body">
      <div id="menu-subtitle">마커이름</div>
      <input class="marker-input" :placeholder="defaultMarkerName" :value="markerName" @input="markerName = $event.target.value">
      <div id="menu-subtitle">마커 제작자의 한마디</div>
      <textarea id="description" :value="description" @input="description = $event.target.value" @keyup="checklength" class="marker-input" placeholder="마커를 설명해 주세요 (최대 200자)" style="height: 200px; resize: none; word-break: break-all; text-overflow: clip;"></textarea>
      <p v-if="DescOver" style="color: rgb(237,40,40)" >최대 200자 까지만 가능합니다!</p>
      <div id="menu-subtitle">태그</div>
      <input class="marker-input" placeholder="#차박#낚시#바베큐 (최대 10개)" style="font-size: 12px;" :value="tagString" @input="tagString = $event.target.value">
    </div>
    <div id="private-selector">나만보기<input type="checkbox" name="isPrivate" v-model="isPrivate"></div> <br>
    <button @click="updateMarker" class="marker-make-button">저장</button>
  </div>
</template>

<script>
import axios from 'axios'
import refresh from '../getRefreshedToken.js'

export default {
  data() {
    return {
      markerName: this.selected.name,
      description: this.selected.description,
      tagString: this.selected.tags,
      isPrivate: this.selected.isPrivate,

      DescOver: false
    }
  },
  props: ['selected'],
  computed: {
    defaultMarkerName: function() {
      return `${sessionStorage.getItem('name')}님의 마커`
    }
  },
  methods: {
    updateEvent: function(marker) {
      this.$emit('updateEvent', marker)
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
    updateMarker: async function() {

      const markerId = this.selected.markerId
      const mfId = this.selected.fisherId
      let name = this.markerName
      const latitude = this.selected.latitude
      const longitude = this.selected.longitude
      const place_addr = this.selected.place_addr
      const isPrivate = this.isPrivate
      const tagString = this.tagString
      let description = this.description

      if (name === '') {
        const userName = sessionStorage.getItem("name")
        name = `${userName}님의 마커`
      }
      try {
        await axios({
          method: 'PUT',
          url: 'https://flarepoint.herokuapp.com/marker/update',
          headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
          data: { markerId: markerId, mfId: mfId, name: name, latitude: latitude, longitude: longitude, place_addr: place_addr, isPrivate: isPrivate, tagString: tagString, description: description },
          withCredentials: true
        }).then((res) => {
          if (res.data.success === false) {
            console.log('marker create failed.' + res.data.response)
          }
          else {
            sessionStorage.setItem("apiToken", refresh(res.headers))
            this.updateEvent(res.data.response)
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

</style>