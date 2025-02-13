<script setup>
import {RouterView} from 'vue-router'
import router from "@/router";
import {useUserStore} from "@/stores/user";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import {ref} from "vue";

const userStore = useUserStore()
let user = userStore.getUser
const activePath = router.currentRoute.value.path
console.log(activePath)

const logout = () => {
  request.get('/logout/' + user.uid).then(res => {
    if (res.code === '200') {
      userStore.logout()
    } else {
      ElMessage.error(res.msg)
    }
  })
}
const menus = userStore.getMenus

const getAvatarHandler = (avatar) => {
  user.avatar = avatar
}

const unread = ref([])
const getUnRead = () => {
  request.get('/message/unread').then(res => {
    unread.value = res.data
  })
}
getUnRead()

const clickRead = () => {
  router.push('/front/MyMessage')
}

</script>

<template>
  <div>
    <!--头部-->
    <div style="display: flex; height: 60px; line-height: 60px; border-bottom: 1px solid #eee; background-color: aliceblue">
      <div style="width: 400px; display: flex; padding-left: 30px">
        <div style="width: 50px">
          <img src="../assets/imgs/sys_icon.png" alt="" style="width: 40px; position: relative; top: 10px; right: 0">
        </div>
        <div style="flex: 1; font-size: 22px; color: dodgerblue;  font-family: 华文琥珀,serif">校园交友平台</div>
      </div>
      <div style="flex: 1">
        <el-menu
            :default-active="activePath"
            mode="horizontal"
            router
            style="border: none; height: 100%"
            background-color="aliceblue"
        >
          <el-menu-item index="/front/home">首页</el-menu-item>
          <el-menu-item index="/front/info">校园公告</el-menu-item>
          <el-menu-item index="/front/dynamic">动态圈子</el-menu-item>
          <el-menu-item index="/front/activity">校园活动</el-menu-item>
          <el-menu-item index="/front/chatRoom">聊天室</el-menu-item>
          <el-menu-item index="/front/user">用户中心</el-menu-item>
          <el-sub-menu index="/front/individual">
            <template #title>个人中心</template>
<!--            <el-menu-item index="/front/individual">个人主页</el-menu-item>-->
            <el-menu-item index="/front/myDynamic">我的动态</el-menu-item>
            <el-menu-item index="/front/myReserve">我的活动预约</el-menu-item>
            <el-menu-item index="/front/myCollect">我的收藏</el-menu-item>
            <el-menu-item index="/front/myFollow">我的关注</el-menu-item>
            <el-menu-item index="/front/myFans">我的粉丝</el-menu-item>
            <el-menu-item index="/front/myMessage">我的消息</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
      <div style="width: 300px; text-align: right; padding-right: 30px">
        <div>
          <el-badge style="margin-top: 10px; margin-right: 30px; cursor: pointer" :value="unread.length" :max="99" @click="clickRead">
            <el-icon style="color: grey" size="25"><Message /></el-icon>
          </el-badge>
          <span style="margin-right: 15px; color: blue; font-size: 16px">{{ user.name }}</span>
          <el-dropdown>
            <el-avatar :size="40" :src="user.avatar" style="margin-top: 10px; cursor: pointer; margin-right: 10px"/>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <div @click="router.push('/front/userDetail?id=' + user.id)">个人主页</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="router.push('/front/person')">个人信息</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="router.push('/front/password')">修改密码</div>
                </el-dropdown-item>
                <el-dropdown-item v-if="user.role === 'ADMIN'">
                  <div @click="router.push('/')">前往后台界面</div>
                </el-dropdown-item>
                <el-dropdown-item>
                  <div @click="logout">退出登录</div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <div style="width: 100%; margin: 10px auto; padding: 10px">
      <!-- 加上随机数，解决界面不跳转的问题 -->
      <RouterView :key="router.currentRoute.value.fullPath + Math.random()"/>
    </div>

  </div>
</template>


<style scoped>
:deep(.el-badge__content.is-fixed) {
  top: 10px !important;
}
</style>