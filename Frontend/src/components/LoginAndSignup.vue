<template>
  <div>
    <transition name="just-fade-in">
      <div id="las-input" v-if="isLogin">
        <div style="display: flex; justify-content: center;">
          <img id="las-logo" :src="InlineLogo" alt="inline logo">
        </div>
        <div>
          <p>이메일</p>
          <input type="email" v-model="principal">
          <p>패스워드</p>
          <input type="password" v-model="credentials" @keydown.enter="login">
          <p style="color: rgb(237,40,40)">{{ login_error }}</p>
        </div>
        <div id="las-footer">
          <button class="las-button" @click="login">로그인</button>
          <button class="las-button" @click="isLogin = false">회원가입</button>
        </div>
      </div>
    </transition>
    <transition name="just-fade-in">
      <div id="las-input" v-if="!isLogin">
        <img class="back" :src="BackButtonSrc" @click="isLogin = true" alt="뒤로가기">
        <div>
          <img class="signup_profile_image" alt="profile image" :src="Image" @click="notiOn = true" >
          <p style="font-size: 9px; color: #cacaca; text-align: center;">이미지 클릭 시 프로필 업로드</p>
          <p>이메일</p>
          <input placeholder="example@example.com" type="email" v-model="principal" @keyup="autoEmailCheck">
          <p v-if="email_error_message !== null" style="color: rgb(237,40,40)">{{ email_error_message }}</p>
          <p v-if="emailChecked" style="color: rgb(75,100,255)">{{ email_success_message }}</p>
          <p>비밀번호</p>
          <input placeholder="영문,숫자,특수문자 포함 8자 이상. 연속된 문자 3자 이상 X" type="password" style="font-size: 7px;" v-model="credentials">
          <p>비밀번호 확인</p>
          <input placeholder="비밀번호를 한번 더 입력해 주세요" type="password" style="font-size: 7px;" v-model="checkCredentials">
          <p>닉네임</p>
          <input placeholder="2자 이상 10자 이하" type="text" :value="name" @input="name = $event.target.value" @keyup="autoNameCheck">
          <p v-if="name_error_message !== null" style="color: rgb(237,40,40)">{{ name_error_message }}</p>
          <p v-if="nameChecked" style="color: rgb(75,100,255)">{{ name_success_message }}</p>
        </div>
        <p style="color: rgb(237,40,40)">{{ signup_error_message }}</p>
        <div id="las-footer">
          <button class="las-button" @click="signup">회원가입</button>
        </div>
      </div>
    </transition>
    <transition name="noti">
			<noti v-if="notiOn" @notiEvent="notiOn = false" :text="'기능 개발 중 입니다'"></noti>
		</transition>
  </div>
</template>

<script>
import axios from 'axios';
import Noti from './Noti.vue'

export default {

  mounted() {

  },

  data() {
    return {
      isLogin: true,

      BackButtonSrc: require("./../assets/back.png"),
      InlineLogo: require("../assets/inline_logo.png"),
      Image: require("../assets/user.png"),

      login_error: null,
      email_error_message: null,
      email_success_message: null,
      name_error_message: null,
      name_success_message: null,

      principal: '',
      credentials: '',
      checkCredentials: '',
      name: '',

      emailChecked: false,
      nameChecked: false,

      COOKIE_NAME: 'refreshToken',

      timer: null,
      notiOn: false
    }
  },
  components: {
    Noti
  },
  computed: {
    signup_error_message: function() {
      if (this.credentials !== this.checkCredentials)
        return "비밀번호와 비밀번호 확인이 일치하지 않습니다."
      else if (this.credentials.length > 0 && this.credentials.length < 8)
        return "비밀번호가 8자 미만입니다."
      else if (this.principal !== '' && this.credentials !== '' && this.principal.includes(this.credentials))
        return "비밀번호가 이메일에 포함되지 않게 해주세요."
      else
        return ''
    }
  },
  methods: {

    //--------------------------- login -----------------------------------

    login: async function() {

      //axios를 이용해서 백에 /login 
      if (this.principal === null || this.credentials === null) {
        this.login_error = "이메일과 비밀번호는 필수로 입력해 주세요!"
      }
      else {
        this.login_error = null
        try {
          await axios({
            method: 'POST',
            url: 'https://flarepoint.herokuapp.com/fisher/login',
            data: { principal: this.principal.trim(), credentials: this.credentials },
            headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
            withCredentials: true
            }).then((res) => { 

            if (res.data.success === false) {
              const statusCode = res.data.error.status
                
            }
            else{
              if (!sessionStorage.getItem("apiToken")){
                sessionStorage.setItem("apiToken", res.data.response.apiToken)
                sessionStorage.setItem("profImageName", res.data.response.fisher.profImageName)
                sessionStorage.setItem("name", res.data.response.fisher.fisherName)
                sessionStorage.setItem("role", res.data.response.fisher.role)
                sessionStorage.setItem("email", res.data.response.fisher.email)
                sessionStorage.setItem("id", res.data.response.fisher.id)
              }
              else {
                sessionStorage.setItem("apiToken",res.data.response.apiToken)
              }

              this.loginEvent(false)
            }
          }).catch((error) => {
            console.warn(error)
            if (error.response.status === 401)
              this.login_error = "비밀번호를 다시 입력해주세요."
            else if (error.response.status === 406)
              this.login_error = "이메일 또는 비밀번호를 다시 확인해주세요."
            else
              this.login_error = "존재하지 않는 이메일입니다."
          })
        } catch (error) {
          this.login_error = "아이디 또는 비밀번호를 잘못 입력하셨습니다."
        }
      }
      
    },

    //-------------------------- signup -----------------------------------

    signup: async function() {
      //회원가입 처리
      // 비밀번호 확인 체크
      if (this.nameChecked === true && this.emailChecked === true && this.credentials === this.checkCredentials && this.credentials.length > 7){
        try {
          await axios({
            method: 'POST',
            url: 'https://flarepoint.herokuapp.com/fisher/join',
            data: { principal: this.principal.trim(), credentials: this.credentials, profImageName: 'default', name: this.name.trim() },
            withCredentials: true
          })
          .then((res) => {
            //apiToken, refreshToken, fisher

            if (res.data.success === false) {
              console.log(`unexpected error occured`)              
            }
            else {
              sessionStorage.setItem("apiToken", res.data.response.apiToken)
              sessionStorage.setItem("name", res.data.response.fisher.fisherName)
              sessionStorage.setItem("role", res.data.response.fisher.role)
              sessionStorage.setItem("email", res.data.response.fisher.email)
              sessionStorage.setItem("id", res.data.response.fisher.id)

              alert(`가입되었습니다! 환영합니다 ${this.name.trim()}님:)`)

              this.isLogin = true
            }
          }).catch ((error) => {
            if (error.response.status === 406) {
              alert("이메일 또는 비밀번호가 형식에 맞지 않습니다.")
            }
          })
        } catch (error) {
          console.error("unexpected error occured" + error)
        }
      }
    },

    //------------------------------------ exist check ---------------------------------------

    //debounce
    autoEmailCheck: function() {
      this.emailChecked = false
      if(this.timer) {
        clearTimeout(this.timer)
      }
      this.timer = setTimeout(() => {
        this.emailCheck()
      }, 600)
    },

    autoNameCheck: function() {
      this.nameChecked = false
      if(this.timer) {
        clearTimeout(this.timer)
      }
      this.timer = setTimeout(() => {
        this.nameCheck()
      }, 600)
    },

    emailCheck: async function() {
      //email 중복체크
      try {
        await axios({
          method: 'POST',
          url: 'https://flarepoint.herokuapp.com/fisher/join/email/exists',
          data: { req: this.principal.trim() }
        })
        .then((res) => {

          if (res.data.success === false) {
            const statusCode = res.data.error.status

            if (statusCode === 409) 
              this.email_error_message = "이미 같은 이메일이 존재합니다.."
          }
          else {
            this.emailChecked = true
            this.email_success_message = "사용 가능한 이메일입니다!"
            this.email_error_message = null
          }
        }).catch((error) => {
          if (error.response.status === 406)
            this.email_error_message = "이메일 형식에 맞지 않습니다."
        })
      } catch (error) {
        console.warn("unexpected error occured" + error)
        this.email_error_message = "서버와의 연결이 좋지 않습니다.."
      } 
    },

    nameCheck: async function() {
      //닉넴 중복체크
      if (this.name.trim().length < 2 || this.name.trim().length > 10) {
        this.name_error_message = "닉네임은 2자 이상 10자 이하여야 입니다."
      }
      else {
        try {
        await axios({
          method: 'POST',
          url: 'https://flarepoint.herokuapp.com/fisher/join/name/exists',
          data: { req: this.name.trim() }
        })
        .then((res) => {

          if (res.data.success === false) {
            const statusCode = res.data.error.status
        
            if (statusCode === 409) 
              this.name_error_message = "이미 같은 닉네임이 존재합니다.."
          }
          else{
            this.nameChecked = true
            this.name_success_message = "사용 가능한 닉네임입니다!"
            this.name_error_message = null
          }
        })
      } catch (error) {
        console.warn("unexpected error occured" + error)
        this.name_error_message = "서버와의 연결이 좋지 않습니다.."
      } 
      }
    },

    // --------------------------------- util --------------------------------------

    loginEvent: function() {
      this.$emit("loginEvent")
    }
  }

}
</script>

<style>

p {
  text-align: left;
  color: black;
  font-size: 13px;
  font-family: Pretendard-Regular;
  margin: 5px;
}

#las-input {
  padding: 30px;
  font-size: 11px;
  display: flex;
  flex-direction: column;
}

#las-logo {
  margin: 20px;
  width: 200px
}

#las-input input {
  width: -webkit-fill-available;
  height: 30px;
  font-size: 14px;
  border: 1px solid #cacaca;
  border-radius: 10px;
}

.signup_profile_image {
  padding: 5px;
	width: 80px;
	height: 80px;
	border-radius: 100%;
  border: 2px solid #cacaca
}
.signup_profile_image:hover {
  cursor: pointer;
}

.back {
  width: 4rem;
  height: 4rem;
  float: left;
}

.back:hover {
  cursor: pointer;
}

#las-footer {
  display: flex; 
  justify-content: space-around; 
  align-items: center;
  margin: 20px 10px 0 10px;
}

.las-button {
  width: 80px;
  height: 40px;
  border-radius: 10px;
  color: white;
  background-color: #F3776B;
  -webkit-transition-duration: 0.4s; /* Safari */
  transition-duration: 0.4s;
  border: 2px solid #F3776B;
  text-align: center;
}

.las-button:hover {
  background-color: rgb(255, 255, 255);
  color: black;
}

@-webkit-keyframes slide-left {
  0% {
    -webkit-transform: translateX(0);
            transform: translateX(0);
  }
  100% {
    -webkit-transform: translateX(-1rem);
            transform: translateX(-1rem);
  }
}
@keyframes slide-left {
  0% {
    -webkit-transform: translateX(0);
            transform: translateX(0);
  }
  100% {
    -webkit-transform: translateX(-1rem);
            transform: translateX(-1rem);
  }
}

</style>