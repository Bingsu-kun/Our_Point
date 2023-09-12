import { createApp } from 'vue'
import App from './App.vue'

const script = document.createElement('script')

script.id = 'kakaoScript'
script.async = true
script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=12658ca88c9c540ede74d413df66f251&libraries=services&autoload=false'
document.body.appendChild(script);

createApp(App).mount('#app')
