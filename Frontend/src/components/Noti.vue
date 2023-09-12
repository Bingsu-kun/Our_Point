<template>
  <div id="noti-footer">
    <div id="noti-modal">
      <div>{{ text }}</div>
    </div>
  </div>
</template>

<script>
export default {
  mounted() {
    setTimeout(() => {
      this.$emit("notiEvent")
    }, 3000)
  },
  props:['text']
}
</script>

<style>

#noti-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}

#noti-modal {
  width: 200px;
  height: 50px;
  background-color: #F3776B;
  color: white;
  border: 0;
  border-radius: 10px;
  font-family: Pretendard-Black;
  font-size: 13px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.noti-enter-active {
  -webkit-animation: noti-in 0.3s ease-out;
  animation: noti-in 0.3s ease-out;
}
.noti-leave-active {
  -webkit-animation: noti-out 0.3s ease-in;
  animation: noti-out 0.3s ease-in;
}

@-webkit-keyframes noti-in {
  0% {
    -webkit-transform: translateY(100%);
            transform: translateY(100%);
            opacity: 0;
  }
  100% {
    -webkit-transform: translateY(0%);
            transform: translateY(0%);
            opacity: 100%;
  }
}
@keyframes noti-in {
  0% {
    -webkit-transform: translateY(100%);
            transform: translateY(100%);
            opacity: 0;
  }
  100% {
    -webkit-transform: translateY(0%);
            transform: translateY(0%);
            opacity: 100%;
  }
}
@-webkit-keyframes noti-out {
  100% {
    -webkit-transform: translateY(100%);
            transform: translateY(100%);
            opacity: 0;
  }
  0% {
    -webkit-transform: translateY(0%);
            transform: translateY(0%);
            opacity: 100%;
  }
}
@keyframes noti-out {
  100% {
    -webkit-transform: translateY(100%);
            transform: translateY(100%);
            opacity: 0;
  }
  0% {
    -webkit-transform: translateY(0%);
            transform: translateY(0%);
            opacity: 100%;
  }
}

</style>