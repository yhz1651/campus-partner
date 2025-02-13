<script setup>
import {reactive, ref} from "vue"
import {User, Lock, Message} from '@element-plus/icons-vue'
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import router from "@/router";

const registerData = reactive({})
const time = ref(0)
const interval = ref(-1)
const ruleFormRef = ref()

const confirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请确认密码'))
  }
  if (registerData.password !== value) {
    callback(new Error('两次输入密码不一致'))
  }
  callback()
}

// 邮箱正则验证
const reg = /^\w+((.\w+)|(-\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/
const checkEmail = (rule, value, callback) => {
  if (!reg.test(value)) {  // test校验输入值
    return callback(new Error('邮箱格式不合法'));
  }
  callback()
}

const rules = reactive({
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
  ],
  email: [
    {validator: checkEmail, trigger: 'blur'},
  ],
  emailCode: [
    {required: true, message: '请输入验证码', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 3, max: 20, message: '密码长度在3-20位之间', trigger: 'blur'},
  ],
  confirmPassword: [
    {validator: confirmPassword, trigger: 'blur'},
  ],
})

const register = () => {
  ruleFormRef.value.validate(valid => {
    if (valid) {
      if (registerData.password !== registerData.confirmPassword) {
        ElMessage.warning('两次密码不一致')
      }
      // 发送表单数据给后台
      request.post('/register', registerData).then(res => {
        if (res.code === '200') {
          ElMessage.success('注册成功')
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

const times = () => {
  // 清空定时器
  if (interval.value >= 0) {
    clearInterval(interval.value)
  }
  time.value = 60
  interval.value = setInterval(() => {
    if (time.value > 0) {
      time.value--
    }
  }, 1000)
}

// 点击发送邮件按钮
const sendEmail = () => {
  if (!reg.test(registerData.email)) {  // test可以校验你的输入值
    ElMessage.warning("请输入合法的邮箱")
    return
  }

  request.get("/email", {
    params: {
      email: registerData.email,
      type: "REGISTER"
    }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('发送成功，有效期5分钟')
      times()  // 倒计时
    } else {
      ElMessage.error(res.msg)
    }
  })
}

</script>

<template>
  <div style="height: 100vh; overflow: hidden; background-image: linear-gradient(-225deg, #77FFD2 0%, #6297DB 48%, #1EECFF 100%);">
    <div style="display: flex; width: 50%; margin: 120px auto; background-color: white;
      box-shadow: 0 0 10px 1px white; overflow: hidden; border-radius: 10px">

      <!--   左侧   -->
      <div style="padding: 50px; flex: 1; display: flex; flex-direction: column; background-color: aliceblue">
        <div style="display: flex;">
          <div >
            <img src="../assets/imgs/sys_icon.png" alt="" style="width: 50px;">
          </div>
          <div style="flex: 1; margin-left: 10px; color: dodgerblue; font-size: 32px;  line-height: 50px; font-family: 华文琥珀,serif">
            校园交友平台
          </div>
        </div>
        <div style="flex: 1;">
          <img src="../assets/imgs/register.png" alt="" style="width: 100%;">
        </div>
      </div>

      <!--   右侧   -->
      <div style="position:relative; width: 400px;">
        <div style="width: 400px; background-color: white; padding: 0 30px; position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); ">
        <el-form
              ref="ruleFormRef"
              :model="registerData"
              :rules="rules"
              size="large"
              status-icon
          >
            <div style="text-align: center; color: dodgerblue; font-size: 30px; font-weight: bold; margin-bottom: 30px">
              注 册
            </div>
            <el-form-item prop="username">
              <el-input v-model="registerData.username" placeholder="请输入用户名" :prefix-icon="User"/>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerData.password" show-password placeholder="请输入密码" :prefix-icon="Lock"/>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="registerData.confirmPassword" show-password placeholder="请确认密码"
                        :prefix-icon="Lock"/>
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="registerData.email" placeholder="请输入邮箱" :prefix-icon="Message"/>
            </el-form-item>
            <el-form-item prop="emailCode">
              <div style="display: flex">
                <el-input style="flex: 1" placeholder="请输入验证码" v-model="registerData.emailCode"></el-input>
                <el-button type="primary" style="width: 120px; margin-left: 10px" @click="sendEmail"
                           :disabled="time > 0">点击发送<span
                    v-if="time">（{{ time }}）</span></el-button>
              </div>
            </el-form-item>
<!--            <el-form-item prop="name">-->
<!--              <el-input v-model="registerData.name" placeholder="请输入昵称（可选）" :prefix-icon="User"/>-->
<!--            </el-form-item>-->
            <el-form-item>
              <el-button type="primary" style="width: 100%" @click="register">注 册</el-button>
            </el-form-item>
            <div style="text-align: right">
              <el-button type="primary" link @click="router.push('/login')">已有账号？请登录</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>
