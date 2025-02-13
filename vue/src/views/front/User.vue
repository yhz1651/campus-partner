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
  Data: [],
})

const search = () => {
  request.get('/user/pageFront', {
    params: {
      name: name.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.Data = res.data.records
    total.value = res.data.total
  })
}

// 好友推荐
const recommend = () => {
  request.get('/user/recommend').then(res => {
    state.Data = res.data
  })
}
recommend()

// 刷新推荐
const refreshUsers = () => {
  request.get('/user/recommend').then(res => {
    state.Data = res.data
  })
}

// 关注
const follow = (id) => {
  request.post('/relation', {userId: id}).then(res => {
    ElMessage.success('关注成功，积分+3')
    search()
  })
}

// 取消关注
const followCancel = (id) => {
  request.post('/relation', {userId: id}).then(res => {
    ElMessage.success('取消关注成功')
    search()
  })
}


</script>

<template>
  <div style="width: 60%; margin: auto; display: flex">
    <!--  内容  -->

    <div style="flex: 1; margin-left: 20px">
      <el-card style="min-height: calc(100vh - 100px)">
        <div style="display: flex; padding-bottom: 10px; border-bottom: 1px solid #ccc;">
          <div style="font-size: 20px; font-weight: bold; color: orange">
            好友推荐
          </div>
<!--          <span style="float: right; top: 5px; font-size: 16px; color: #888888; margin-left: 20px; cursor: pointer" class="refresh" @click="refreshUsers">-->
<!--            <el-icon style="top: 2px"><Refresh /></el-icon> -->
<!--            换一换-->
<!--          </span>-->
          <div style="flex: 1; text-align: right; margin-right: 20px">
            <el-input v-model="name" placeholder="请输入关键词" style="width: 200px" clearable/>
            <el-button type="primary" class="ml5" @click="search">
              <el-icon style="vertical-align: middle">
                <Search/>
              </el-icon>
              <span style="vertical-align: middle"> 搜索 </span>
            </el-button>
          </div>

        </div>

        <!--    有数据    -->
        <div v-if="state.Data" v-for="item in state.Data" :key="item.id" style="border-bottom: 1px solid #ddd; padding: 10px;">
          <!-- 发表人头像、名字、关注  -->
          <div style="display: flex; margin: 10px 0 10px 10px;">
            <!-- 头像 -->
            <img :src="item.avatar" alt="" style="width: 40px; height: 40px; border-radius: 50%; margin-top: 5px; cursor: pointer" @click="router.push('/front/userDetail?id=' + item.id)">

            <!-- 姓名 简介 -->
            <div style="display: flex; flex: 1; flex-direction: column; cursor: pointer" @click="router.push('/front/userDetail?id=' + item.id)">
              <div style="display: flex;">
                <!--   名字   -->
                <div style=" margin-left: 10px;  text-align: left">
                  <span style="font-weight: bold; font-size: 16px">{{ item.name }}</span>
                </div>
                <!--  积分等级  -->
                <div style="text-align: left">
                    <span style="margin-left: 5px">
                      <el-icon :size="20" style="top: 4px">
                        <img style="width: 20px" src="../../assets/imgs/trophy.png" alt="">
                      </el-icon>
                      <span style="color: orange">{{ item.score }}</span>
                    </span>
                </div>
                <!--  关注 粉丝牌  -->
                <div style="margin-left: 5px" v-if="item.hasFollow">
                  <el-tag type="primary">我的关注</el-tag>
                </div>
                <div style="margin-left: 5px" v-if="item.hasBeFollow">
                  <el-tag type="danger">我的粉丝</el-tag>
                </div>

              </div>

              <div style="display: flex; color: #888; margin-left: 10px">
                {{ item.profile }}
              </div>

            </div>

            <!-- 关注  -->
            <div style="display: flex; margin-right: 20px; align-items: center; " v-if="item.id !== user.id">
              <!-- 互相关注  -->
              <div style="display: flex" v-if="item.hasFollow && item.hasBeFollow">
                <el-popconfirm title="您确定取消关注吗？" @confirm="followCancel(item.id)">
                  <template #reference>
                    <el-button type="success" style="font-size: 16px;">互相关注</el-button>
                  </template>
                </el-popconfirm>
              </div>

              <!-- 已关注  -->
              <div style="display: flex" v-else-if="item.hasFollow">
                <el-popconfirm title="您确定取消关注吗？" @confirm="followCancel(item.id)">
                  <template #reference>
                    <el-button type="primary" style="font-size: 16px;">已关注</el-button>
                  </template>
                </el-popconfirm>
              </div>

              <!--  未关注  -->
              <div v-else>
                <el-button type="danger" style="font-size: 16px;" @click="follow(item.id)">关注</el-button>
              </div>
            </div>
          </div>
        </div>

        <!--    无数据    -->
        <div v-else style="text-align: center; margin: 10px 0;">
          <img src="../../assets/imgs/nodynamic.png" alt="" style="width: 300px">
          <div style="font-size: 26px">暂无结果</div>
        </div>
      </el-card>

      <!--    动态分页    -->
      <el-card v-if="total" style="margin: 10px 0; background-color: white">
        <el-pagination
            @current-change="search"
            @size-change="search"
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