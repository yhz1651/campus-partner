<script setup>
import {useUserStore} from "@/stores/user";
import router from "@/router";

const userStore = useUserStore()
const auths = userStore.getAuths
const user = userStore.getUser

// 返回主页，根据身份返回不同的主页
const back = () => {
  if (user.role === 'ADMIN') {
    router.push('/')
  } else {
    router.push('/front/')
  }
}

</script>

<template>
  <div style="text-align: center;" >
    <img src="../assets/imgs/404.png" alt="" style="height: calc(100vh - 200px); padding: 10px">
<!--    <h1 style="font-size: 150px">404</h1>-->
<!--    <h2>未找到页面</h2>-->
    <div style="margin: 50px">
      <el-button link style="font-size: 50px; height: 60px" size="large" type="primary" @click="back">返回主页</el-button>
    </div>

  </div>
</template>
