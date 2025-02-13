<script setup>
import {nextTick, onMounted, reactive, ref} from "vue"
import {User, Lock} from '@element-plus/icons-vue'
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import {useUserStore} from "@/stores/user";
import router, {setRoutes} from "@/router";
import SIdentify from '../components/Sidentify.vue';

// 图形验证码
let identifyCodes = "1234567890"
let identifyCode = ref('')
const failCount = ref(0)
const passwordForm = reactive({})
const ruleFormRef = ref()
const rulePasswordFormRef = ref()
const passwordVis = ref(false)
const interval = ref(-1)
const time = ref(0)
const loginData = reactive({})


const randomNum = (min, max) => {
  return Math.floor(Math.random() * (max - min) + min)
}
const makeCode = (o, l) => {
  for (let i = 0; i < l; i++) {
    identifyCode.value += o[randomNum(0, o.length)];
  }
}
const refreshCode = () => {
  identifyCode.value = "";
  makeCode(identifyCodes, 4);
}
// 生成验证码
onMounted(() => {
  identifyCode.value = "";
  makeCode(identifyCodes, 4);
})


const rules = reactive({
  username: [
    {required: true, message: '请输入账号', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 3, max: 20, message: '密码长度在3-20位之间', trigger: 'blur'},
  ],
})

const passwordRules = reactive({
  email: [
    {required: true, message: '请输入账号', trigger: 'blur'},
  ],
  emailCode: [
    {required: true, message: '请输入验证码', trigger: 'blur'},
  ],
})

const login = () => {
  ruleFormRef.value.validate(valid => {
    if (valid) {
      // 失败1次触发验证码
      if (failCount.value >= 1 && loginData.code !== identifyCode.value) {
        ElMessage.warning('验证码错误')
        return
      }
      // 发送表单数据给后台
      request.post('/login', loginData).then(res => {
        if (res.code === '200') {
          ElMessage.success('登录成功')
          const userStore = useUserStore()
          userStore.setManagerInfo(res.data) // 将登录信息存到缓存中
          if (res.data.user.role === 'USER') {
            router.push('/front')
          } else {
            router.push('/')
          }
        } else {
          ElMessage.error(res.msg)
          failCount.value++  // 失败次数加1
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

// 发送邮件
const sendEmail = () => {
  const reg = /^\w+((.\w+)|(-\w+))@[A-Za-z0-9]+((.|-)[A-Za-z0-9]+).[A-Za-z0-9]+$/
  if (!reg.test(passwordForm.email)) {  // test可以校验你的输入值
    ElMessage.warning("请输入合法的邮箱")
    return
  }

  request.get("/email", {
    params: {
      email: passwordForm.email,
      type: "RESETPASSWORD"
    }
  }).then(res => {
    if (res.code === '200') {
      times()  // 倒计时
      ElMessage.success('发送成功，有效期5分钟')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 点击忘记密码触发
const handleResetPassword = () => {
  passwordVis.value = true
  // 触发表单重置
  nextTick(() => {   // 下个时钟触发
    rulePasswordFormRef.value.resetFields()
  })
}

// 调用新接口重置密码
const resetPassword = () => {
  rulePasswordFormRef.value.validate(valid => {
    if (valid) {
      request.post("/password/reset", passwordForm).then(res => {
        if (res.code === '200') {
          ElMessage.success('重置成功，您的密码为：' + res.data)
          passwordVis.value = false
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

</script>

<template>
  <div style="height: 100vh; overflow: hidden; background-image: linear-gradient(-225deg, #77FFD2 0%, #6297DB 48%, #1EECFF 100%); ">

    <div style="display: flex; width: 50%; margin: 100px auto; background-color: white;
      box-shadow: 0 0 10px 1px white; overflow: hidden; border-radius: 10px; ">
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
          <img src="../assets/imgs/login.png" alt="" style="width: 100%;">
        </div>

      </div>

      <!--   右侧   -->
      <div style="position:relative; width: 400px;">
        <div style="padding: 30px; color: #333;">
          <div>欢迎来到</div>
          <div style="font-size: 40px; color: #af8df2; font-weight: bold; font-family: 华文彩云,serif">校园交友平台</div>
        </div>

        <div style="width: 400px; background-color: white; padding: 0 30px; position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); ">
          <!--   登录注册   -->
          <div>
            <el-form
                ref="ruleFormRef"
                :model="loginData"
                :rules="rules"
                size="large"
                status-icon
            >
              <div style="text-align: center; color: dodgerblue; font-size: 30px; font-weight: bold; margin-bottom: 40px">
                登 录
              </div>
<!--              <div style="padding-left: 5px; padding-bottom: 5px; color: grey">用户名</div>-->
              <el-form-item prop="username">
                <el-input v-model="loginData.username" placeholder="请输入用户名或邮箱" :prefix-icon="User"/>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginData.password" show-password placeholder="请输入密码" :prefix-icon="Lock"/>
              </el-form-item>
              <div style="display: flex; margin: 15px 0" v-if="failCount >= 1">
                <div style="flex: 1">
                  <el-input v-model="loginData.code" placeholder="验证码"></el-input>
                </div>
                <div>
                  <div @click="refreshCode" style="margin-left: 5px">
                    <SIdentify :identifyCode="identifyCode"/>
                  </div>
                </div>
              </div>
              <el-form-item>
                <el-button type="primary" style="width: 100%; margin-top: 5px" @click="login">登 录</el-button>
              </el-form-item>
            </el-form>


            <!--   忘记密码 注册   -->
            <div>
              <el-button type="info" link style="float: left" @click="handleResetPassword">忘记密码</el-button>
              <el-button type="primary" link @click="router.push('/register')" style="float: right">没有账号？请注册
              </el-button>
            </div>
          </div>
        </div>

      </div>
    </div>

    <el-dialog v-model="passwordVis" title="忘记密码" :close-on-click-modal="false"
               style="width: 500px; padding: 0 20px">
      <el-form :model="passwordForm" ref="rulePasswordFormRef" :rules="passwordRules" status-icon label-width="70px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="passwordForm.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="验证码" prop="emailCode">
          <div style="display: flex; width: 100%">
            <el-input style="flex: 1" v-model="passwordForm.emailCode" clearable></el-input>
            <el-button style="width: 120px; margin-left: 5px" @click="sendEmail" :disabled="time > 0">点击发送<span v-if="time">（{{ time }}）</span></el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="passwordVis = false">取消</el-button>
        <el-button type="primary" @click="resetPassword">确认</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>
