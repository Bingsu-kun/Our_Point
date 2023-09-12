<template>
	<div id="user-profile-container">
		<div id="menu-title">프로필</div>
		<div id="profile">
			<div id="image-control">
				<img alt="profile image" :src="Image">
				<div>
					<span class="prof-img-btn" @click="notiOn = true; notiText = '기능 구현 중 입니다'">편집</span>
					<span>|</span>
					<span class="prof-img-btn" @click="notiOn = true; notiText = '기능 구현 중 입니다'">지우기</span>
				</div>
			</div>
			<div id="user-properties">
				<div>닉네임 | {{ Name }}</div>
				<div>이메일 | {{ Email }}</div>
				<div>내가 만든 마커 | {{ Markers }}</div>
				<div>내가 받은 좋아요 | {{ Likes }}</div>
			</div>
		</div>
		<div v-if="Editting" id="edit-container">
			<div style="display: flex; justify-content: center;">
				<div id="edit-title">닉네임 변경</div>
			</div>
			<div style="display: flex; flex-direction: column;">
				<input :value="EditNick" @input="EditNick = $event.target.value" id="edit-input" placeholder="변경할 닉네임을 입력해 주세요">
				<div v-if="nickNameError !== ''" id="edit-error">{{ nickNameError }}</div>
			</div>
			<div style="display: flex; justify-content: center;">
				<button class="edit-btn" @click="changeName">변경</button>
			</div>
		</div>
		<div v-if="Editting" id="edit-container">
			<div style="display: flex; justify-content: center; align-items: start;">
				<div id="edit-title">비밀번호 변경</div>
			</div>
			<div style="display: flex; flex-direction: column;">
				<input :value="Pwd" @input="Pwd = $event.target.value" id="edit-input" placeholder="기존 비밀번호를 입력해 주세요">
				<div id="edit-error" style="color: grey">영어, 숫자, 특수문자를 포함한 8자 이상</div>
				<input :value="EditPwd" @input="EditPwd = $event.target.value" id="edit-input" placeholder="새 비밀번호를 입력해 주세요">
				<input :value="EditPwdCheck" @input="EditPwdCheck = $event.target.value" id="edit-input" placeholder="새 비밀번호를 한번 더 입력해 주세요">
				<div v-if="pwdError !== ''" id="edit-error">{{ pwdError }}</div>
			</div>
			<div style="display: flex; justify-content: center; align-items: end;">
				<button class="edit-btn" @click="changePassword">변경</button>
			</div>
		</div>
		<div style="display: flex; justify-content: space-around; align-item: center; padding: 10px 40px;">
			<button class="prof-btn" @click="logout" >로그아웃</button>
			<button v-if="!Editting" class="prof-btn" @click="Editting = true">수정</button>
			<button class="prof-btn" @click="menuCloseEvent" >닫기</button>
		</div>
		<transition name="noti">
			<noti v-if="notiOn" @notiEvent="notiOn = false" :text="notiText"></noti>
		</transition>
	</div>
</template>

<script>
import axios from 'axios';
import Noti from './Noti.vue'

export default {

	mounted() {
		this.getReceivedLikes()
	},
	data() {
			return {
				Name: sessionStorage.getItem('name'),
				Image: require('../assets/user.png'),
				Email: sessionStorage.getItem('email'),
				Markers: JSON.parse(sessionStorage.getItem('my')).length,
				Likes: 0,

				Editting: false,

				EditNick: '',
				NickDuplicated: false,
				Pwd: '',
				EditPwd: '',
				EditPwdCheck: '',
				PwdRegError: false,
				PwdNotMatch: false,

				notiOn: false,
				notiText: ''
			}
	},
	components: {
		Noti
	},
	computed: {
		nickNameError: function() {
			if (this.EditNick === '')
				return ''
			else if (this.EditNick === this.Name)
				return '기존 닉네임과 동일합니다'
			else if (this.NickDuplicated)
				return '이미 같은 닉네임이 존재합니다'
			else if (this.EditNick < 2 || this.EditNick > 10)
				return '너무 짧거나 깁니다'
			else
				return ''
		},
		pwdError: function() {
			if (this.EditPwd === '')
				return ''
			else if (this.EditPwd.length < 8)
				return '비밀번호가 너무 짧습니다'
			else if (this.EditPwd !== this.EditPwdCheck)
				return '새로운 비밀번호와 비밀번호 확인이 일치하지 않습니다'
			else if (this.PwdRegError)
				return '비밀번호 형식이 일치하지 않습니다'
			else if (this.PwdNotMatch)
				return '기존 비밀번호가 일치하지 않습니다'
			else
				return ''
		}
	},
	methods: {
		getReceivedLikes: async function() {
      try {
        await axios({
          method: 'GET',
          url: 'https://flarepoint.herokuapp.com/marker/mylikecount',
          headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
          withCredentials: true
        }).then((res) => {

          if (res.data.success === false) {
            console.log('get received likes failed.')
						this.Likes = '가져오기 실패!'
          }
          else {
            this.Likes = res.data.response
          }
        })
      } catch (error) {
        this.Likes = '가져오기 실패!'
      }
    },
		changeName: async function() {
			const name = this.EditNick
			try {
				await axios({
					method: 'PUT',
					url: 'https://flarepoint.herokuapp.com/fisher/me/name/change',
					headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
					data: { credentials: '', changeValue: name },
					withCredentials: true
				}).then((res) => {

					if (res.data.success === false) {
						console.log('change name failed.')
					}
					else {
						this.notiOn = true
						this.notiText = '성공적으로 변경되었습니다'
						this.NickDuplicated = false
						this.Name = name
						this.EditNick = ''
						sessionStorage.setItem("name",name)
					}
				}).catch((error) => {
					if (error.response === 406)
						this.NickDuplicated = true
					else
						console.warn(error)
				})
			} catch (error) {
				console.log('change name failed.')
			}
		},
		changePassword: async function() {
			try {
					await axios({
						method: 'GET',
						url: 'https://flarepoint.herokuapp.com/fisher/me/password/change',
						headers: { Authorization: `Bearer ${sessionStorage.getItem('apiToken')}` },
						data: { credentials: this.Pwd, changeValue: this.EditPwd },
						withCredentials: true
					}).then((res) => {

						if (res.data.success === false) {
							console.log('change password failed.')
						}
						else {
							this.notiOn = true
							this.notiText = '성공적으로 변경되었습니다'
							this.PwdRegError = false
							this.PwdNotMatch = false
						}
					}).catch((error) => {
						if (error.response === 401)
							this.PwdNotMatch = true
						else if (error.response === 406)
							this.PwdRegError = true
						else
							console.warn(error)
					})
				} catch (error) {
					console.log('change password failed.')
				}
		},
		logout: function() {
			this.$emit("logout")
		},
		menuCloseEvent: function() {
			this.$emit("menuCloseEvent")
		}
	}
}
</script>

<style>
#user-profile-container {
	padding: 20px;
}

#profile {
	padding: 20px 0;
	width: -webkit-fill-available;
	display: grid;
	grid-template-columns: 1fr 3fr;
	justify-content: center;
	align-content: center;
	align-items: center;
}

#profile img {
	padding: 5px;
	width: 80px;
	height: 80px;
	border-radius: 100%;
}

#profile div {
	padding: 0 5px;
}

#image-control {
	display: flex;
	justify-content: center;
	align-items: center;
	align-content: center;
	flex-direction: column;
	font-size: 11px;
}
.prof-img-btn {
	margin: 0 3px;
}
.prof-img-btn:hover {
	cursor: pointer;
}

#user-properties {
	font-size: 13px;
}

#edit-container {
	display: grid;
	grid-template-columns: 2fr 3fr 1fr;
	margin: 10px 0;
}
#edit-title {
	margin: 3px 0;
	padding: 4px 0;
	font-size: 13px;
	font-family: Pretendard-Bold;
}
#edit-input {
	width: -webkit-fill-available;
	margin: 3px 0;
  height: 20px;
  font-size: 9px;
  border: 1px solid #cacaca;
  border-radius: 10px;
}
#edit-error {
	margin: 3px 0;
	font-size: 6px;
	color: #F3776B;
}
.edit-btn {
	margin: 3px 0;
	width: 40px;
	height: 24px;
	border: 0;
	border-radius: 10px;
	color: white;
	background-color: #F3776B;
	transition-duration: 0.3s;
	font-family: Pretendard-Bold;
}
.edit-btn:hover {
	background-color: white;
	color: #F3776B;
	border: 0.5px solid #cacaca;
}

.prof-btn {
	width: 90px;
	height: 40px;
	border: 0.5px solid #cacaca;
	border-radius: 10px;
	background-color: white;
	transition-duration: 0.3s;
}
.prof-btn:hover {
	background-color: #F3776B;
	color: white;
	border: 0;
}
</style>