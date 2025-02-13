<script setup>
import router from "@/router";
import request from "@/utils/request";
import {nextTick, onBeforeUnmount, onMounted, reactive, ref, shallowRef} from "vue";
import {ElMessage} from "element-plus";
import {useUserStore} from "@/stores/user";
import '@wangeditor/editor/dist/css/style.css' // 引入 css

const userStore = useUserStore()
let user = userStore.getUser
const activePath = router.currentRoute.value.path

const name = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const state = reactive({
  tableData: [],
})

const load = () => {
  request.get('/relation/pageFans', {
    params: {
      name: name.value,
      type: 'user',
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.tableData = res.data.records
    total.value = res.data.total
  })
}
load()  // 调用 load方法拿到后台数据

// 关注
const follow = (id) => {
  request.post('/relation', {userId: id}).then(res => {
    ElMessage.success('关注成功，积分+3')
    load()
  })
}

// 取消关注
const followCancel = (id) => {
  request.post('/relation', {userId: id}).then(res => {
    ElMessage.success('取消关注成功')
    load()
  })
}


</script>

<template>
  <div style="width: 60%; margin: auto; display: flex">
    <!--  目录  -->
    <div style="width: 200px; border-radius: 20px">
      <el-card>
        <el-menu
            :default-active="activePath"
            mode="vertical"
            router
            style="border: none; height: 100%"
        >
          <el-menu-item index="/front/myDynamic">我的动态</el-menu-item>
          <el-menu-item index="/front/myReserve">我的活动预约</el-menu-item>
          <el-menu-item index="/front/myCollect">我的收藏</el-menu-item>
          <el-menu-item index="/front/myFollow">我的关注</el-menu-item>
          <el-menu-item index="/front/myFans">我的粉丝</el-menu-item>
          <el-menu-item index="/front/myMessage">我的消息</el-menu-item>
        </el-menu>
      </el-card>
    </div>

    <!--  内容  -->

    <div style="flex: 1; margin-left: 20px">
      <el-card style="min-height: calc(100vh - 100px)">
        <div style="display: flex; padding-bottom: 10px; border-bottom: 1px solid #ccc;">
          <div style="font-size: 20px; font-weight: bold; color: orange">
            我的粉丝
          </div>
        </div>

        <!--    有数据    -->
        <div v-if="total" v-for="item in state.tableData" :key="item.id" style="border-bottom: 1px solid #ddd; padding: 10px;">
          <!-- 发表人头像、名字、关注  -->
          <div style="display: flex; margin: 10px 0 10px 10px;">
            <!-- 头像 -->
            <img :src="item.user.avatar" alt="" style="width: 40px; height: 40px; border-radius: 50%; margin-top: 5px; cursor: pointer" @click="router.push('/front/userDetail?id=' + item.user.id)">

            <!-- 姓名 简介 -->
            <div style="display: flex; flex: 1; flex-direction: column; cursor: pointer" @click="router.push('/front/userDetail?id=' + item.user.id)">
              <div style="display: flex;">
                <!--   名字   -->
                <div style=" margin-left: 10px;  text-align: left">
                  <span style="font-weight: bold; font-size: 16px">{{ item.user.name }}</span>
                </div>
                <!--  积分等级  -->
                <div style="text-align: left">
                    <span style="margin-left: 5px">
                      <el-icon :size="20" style="top: 4px">
                        <img style="width: 20px" src="../../assets/imgs/trophy.png" alt="">
                      </el-icon>
                      <span style="color: orange">{{ item.user.score }}</span>
                    </span>
                </div>
                <!--  关注 粉丝牌  -->
                <div style="margin-left: 5px" v-if="item.user.hasFollow">
                  <el-tag type="primary">我的关注</el-tag>
                </div>
                <div style="margin-left: 5px" v-if="item.user.hasBeFollow">
                  <el-tag type="danger">我的粉丝</el-tag>
                </div>

              </div>

              <div style="display: flex; color: #888; margin-left: 10px">
                {{ item.user.profile }}
              </div>

            </div>

            <!-- 关注  -->
            <div style="display: flex; margin-right: 20px; align-items: center; " v-if="item.user.id !== user.id">
              <!-- 互相关注  -->
              <div style="display: flex" v-if="item.user.hasFollow && item.user.hasBeFollow">
                <el-popconfirm title="您确定取消关注吗？" @confirm="followCancel(item.user.id)">
                  <template #reference>
                    <el-button type="success" style="font-size: 16px;">互相关注</el-button>
                  </template>
                </el-popconfirm>
              </div>

              <!-- 已关注  -->
              <div style="display: flex" v-else-if="item.user.hasFollow">
                <el-popconfirm title="您确定取消关注吗？" @confirm="followCancel(item.user.id)">
                  <template #reference>
                    <el-button type="primary" style="font-size: 16px;">已关注</el-button>
                  </template>
                </el-popconfirm>
              </div>

              <!--  未关注  -->
              <div v-else>
                <el-button type="danger" style="font-size: 16px;" @click="follow(item.user.id)">关注</el-button>
              </div>

            </div>
          </div>
        </div>

        <!--    无数据    -->
        <div v-else style="text-align: center; margin: 10px 0;">
          <img src="../../assets/imgs/nodynamic.png" alt="" style="width: 300px">
          <div style="font-size: 26px">暂无粉丝</div>
          <div style="cursor: pointer; color: dodgerblue" @click="router.push('/front/dynamic')">快去发表一个动态吸引粉丝吧~</div>
        </div>
      </el-card>

      <!--    动态分页    -->
      <el-card v-if="total" style="margin: 10px 0; background-color: white">
        <el-pagination
            @current-change="load"
            @size-change="load"
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            background
            layout="total, prev, pager, next"
            :total="total"
        />
      </el-card>
    </div>
  </div>
</template>


<style scoped>

</style>