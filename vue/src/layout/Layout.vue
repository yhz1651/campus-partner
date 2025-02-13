<script setup>
import {RouterView} from 'vue-router'
import router from "@/router";
import {useUserStore} from "@/stores/user";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import {reactive, ref} from "vue";
import {useRoute} from "vue-router"


const userStore = useUserStore()
let user = userStore.getUser
const activePath = router.currentRoute.value.path.replace('/', '')

const menus = userStore.getMenus
const route = useRoute()
const path = route.path
let pathName = ref('')

const load = () => {
  request.get('/permission/pathName',{
    params: {
      str: path
    }
  }).then(res => {
    if (res.code === '200') {
      pathName.value = res.data // 赋值
    } else {
      ElMessage.error(res.msg)
    }
  })
}
load()


const logout = () => {
  request.get('/logout/' + user.uid).then(res => {
    if (res.code === '200') {
      userStore.logout()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

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
  router.push('/MyMessage')
}

</script>

<template>
  <div>
    <div style="display: flex">

      <!--  导航栏  -->
      <div style="width: 200px; min-height: 100vh; border-right: 1px solid #ccc; background-color:rgb(48, 65, 86)">
        <div
            style="width: 180px; color: aliceblue; font-weight: bold;  text-align: center; font-size: 18px; margin-bottom: 10px; margin-top: 10px">
          <img src="../assets/imgs/admin_icon.png" alt=""
               style="width: 30px; position: relative; top: 8px; margin-right: 5px">
          后台管理系统
        </div>
        <!--      :default-openeds="menus.map(v => v.id + '')"        -->
        <el-menu
            :default-active="activePath"
            :default-openeds="menus.map(v => v.id + '')"
            background-color="rgb(48, 65, 86)"
            text-color="#fff"
            active-text-color="#ffd04b"
            style="border: none"
            router
        >
          <div v-for="item in menus" :key="item.id">
            <div v-if="item.type === 2">
              <el-menu-item :index="item.path" v-if="!item.hide">
                <el-icon v-if="item.icon">
                  <component :is="item.icon"></component>
                </el-icon>
                <span>{{ item.name }}</span>
              </el-menu-item>
            </div>
            <div v-else>
              <el-sub-menu :index="item.id + ''" v-if="!item.hide">
                <template #title>
                  <el-icon v-if="item.icon">
                    <component :is="item.icon"></component>
                  </el-icon>
                  <span>{{ item.name }}</span>
                </template>
                <div v-for="subItem in item.children" :key="subItem.id">
                  <el-menu-item :index="subItem.path" v-if="!subItem.hide">
                    <template #title>
                      <el-icon v-if="subItem.icon">
                        <component :is="subItem.icon"></component>
                      </el-icon>
                      <span>{{ subItem.name }}</span>
                    </template>
                  </el-menu-item>
                </div>
              </el-sub-menu>
            </div>
          </div>
        </el-menu>
      </div>

      <!--  头部  -->
      <div style="flex: 1; display: flex; flex-direction: column">
        <div style="height: 60px; line-height: 60px; min-width: calc(100vw - 200px);   border-bottom: 1px solid #ccc; background-color: aliceblue;">
          <div style="display: flex">
            <div style="flex: 1; padding-left: 10px; margin-left: 10px">
              <span style="font-size: 20px; font-weight: bold;">{{ pathName }}</span>

<!--              <el-breadcrumb separator="/" style="display: inline-block; margin-left: 10px">-->
<!--                <el-breadcrumb-item :to="'/'">首页</el-breadcrumb-item>-->
<!--                <el-breadcrumb-item>{{ pathName }}</el-breadcrumb-item>-->
<!--              </el-breadcrumb>-->
            </div>
            <div style="width: 300px; text-align: right; padding-right: 30px; font-size: 16px; font-weight: bold;">
              <el-badge style="margin-top: 10px; margin-right: 30px; cursor: pointer" :value="unread.length" :max="99" @click="clickRead">
                <el-icon style="color: grey" size="25"><Message /></el-icon>
              </el-badge>
              <span style="margin-right: 15px; color: blue">{{ user.name }}</span>
              <el-dropdown>
                <!-- 头像 -->
                <el-avatar :size="40" :src="user.avatar" style="margin-top: 10px; cursor: pointer; margin-right: 10px"/>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>
                      <div @click="router.push('/person')">个人信息</div>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <div @click="router.push('/password')">修改密码</div>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <div @click="router.push('/front')">前往前台界面</div>
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

        <!--  路由  -->
        <div style="flex: 1; display: flex">
          <div style="flex: 1; min-width: calc(100vw - 200px); width: 0; padding: 10px;">
            <RouterView @getAvatar="getAvatarHandler" :key="router.currentRoute.value.fullPath + Math.random()"/>
          </div>
        </div>

      </div>

    </div>

  </div>
</template>

<style scoped>
:deep(.el-badge__content.is-fixed) {
  top: 10px !important;
}
</style>

