<template>
  <div id="comment-container">
    <img style="border-radius: 100%; width: 100%; border: 1px solid #cacaca" alt="profimg" :src="userProfImg">
    <div style="margin: 0 10px; font-size: 13px; text-align: left;">{{ userName }} | {{ date }}</div>
    <div/>
    <p style="margin: 0 10px; font-size: 11px; text-align: left; white-space: pre-line;">{{ comment }}</p>
  </div>  
</template>

<script>
export default {
  data() {
    return {
      userProfImg: require("../assets/user.png")
    }
  },
  props: ['userProfImgName','userName','date','comment'],
  methods: {
    getUserProfImg: async function() {
      let img = null
      // 대충 S3에서 이미지 가져와야댐.
      
      if (img === null)
        return require("../assets/user.png")
      else
        return img
    }
  }
}
</script>

<style>

#comment-container {
  width: inherit;
  height: fit-content;
  display: grid;
  grid-template-columns: 1fr 7fr;
  grid-template-rows: 1fr 2fr;
  align-items: center;
}


</style>