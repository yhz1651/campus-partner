<script setup>
import {reactive, ref} from "vue";
import router from "@/router"
import request from "@/utils/request";
import {ElMessage} from "element-plus";

const getUrl = (name) => {
  return new URL(`../../assets/imgs/${name}`, import.meta.url).href
}
const imgs = ref([
  getUrl('campus1.jpg'),
  getUrl('campus2.jpg'),
  getUrl('campus3.jpg'),
  getUrl('campus4.jpg'),
  getUrl('campus5.jpg'),
])

const state = reactive({
  hots: [],
  dynamics: [],
  infos: []
})


const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const load = () => {
  request.get('/dynamic/page', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.dynamics = res.data.records
    total.value = res.data.total
  })
  request.get('/dynamic/recommend').then(res => {
    state.hots = res.data
  })
  request.get('/information').then(res => {
    state.infos = res.data
  })
  request.get('/activity').then(res => {
    state.activities = res.data
  })
  request.get('/user/hot').then(res => {
    state.users = res.data
  })
}
load()  // 调用 load方法拿到后台数据

// 刷新推荐
const refreshDynamics = () => {
  request.get('/dynamic/recommend').then(res => {
    state.hots = res.data
  })
}
</script>

<template>
  <div style="width: 60%; margin: auto;">
    <div style="display: flex;">
      <!-- 轮播图 -->
      <el-card style="width: 100%">
        <el-carousel :interval="5000" arrow="always" height="500px">
          <el-carousel-item v-for="item in imgs" :key="item">
            <img :src="item" alt="" style="width: 100%">
          </el-carousel-item>
        </el-carousel>
      </el-card>
    </div>

    <div style="display: flex; margin: 10px 0;">
      <!--    热门动态    -->
      <div style="width: 60%; color: #666">
        <!--    动态列表    -->
        <el-card>
          <div style="padding-bottom: 10px; border-bottom: 1px solid #ccc;">
            <span style="font-size: 20px; font-weight: bold; color: orangered">动态推荐</span>
<!--            <span style=" font-size: 16px; color: #888888; margin-left: 20px; cursor: pointer" class="refresh" @click="refreshDynamics">-->
<!--              <el-icon style="top: 2px"><Refresh /></el-icon> -->
<!--              换一换-->
<!--            </span>-->
            <el-button link style="float: right; top: 5px; font-size: 14px; color: dodgerblue" @click="router.push('/front/dynamic')">
              查看更多
            </el-button>
          </div>

          <div v-for="item in state.hots" :key="item.id"
               style="border-bottom: 1px solid #ddd; padding: 20px; cursor: pointer"
               @click="router.push('/front/dynamicDetail?id=' + item.id)">

            <div>
              <div style="display: flex">
                <!--   头像   -->
                <img v-if="item.user" :src="item.user.avatar" alt="" style="width: 40px; height: 40px; border-radius: 50%">
                <!--   名字   -->
                <div style=" line-height: 40px; margin-left: 10px; " v-if="item.user">
                  <span style="font-weight: bold; font-size: 16px">{{ item.user.name }}</span>
                </div>
                <!--  积分奖杯  -->
                <div style="top: 5px">
                  <span style="margin-left: 5px">
                    <el-icon :size="20" style="top: 4px">
                      <img style="width: 20px" src="../../assets/imgs/trophy.png" alt="">
                    </el-icon>
                    <span style="color: orange">{{ item.user.score }}</span>
                  </span>
                </div>
                <!--  关注 粉丝牌  -->
                <div style="margin-left: 5px; top: 7px" v-if="item.user.hasFollow">
                  <el-tag type="primary">我的关注</el-tag>
                </div>
                <div style="margin-left: 5px; top: 7px" v-if="item.user.hasBeFollow">
                  <el-tag type="danger">我的粉丝</el-tag>
                </div>

              </div>
              <div style="flex: 1;">

              </div>
            </div>

            <!--  动态标题和热度    -->
            <div style="margin: 10px 0; display: flex;">
              <!--  动态标题    -->
              <div style="color: #000; font-size: 18px; font-weight: bold">{{ item.name }}</div>

            </div>

            <!--   内容   -->
            <div style="display: flex; align-items: flex-end">
              <div style="flex: 1" class="dynamic-content" v-html="item.content"></div>
            </div>

            <!--  动态图片  -->
            <div style="margin-top: 10px">
              <el-row :gutter="10">
                <el-col :span="8" style="margin-bottom: 10px" v-for="(img,index) in item.imgs" :key="img" >
                  <el-image style="width: 100%; height: 180px" :src="img.url" v-if="index < 8"></el-image>
                  <img style="width: 100%; height: 180px" src="../../assets/imgs/ellipsis.png" v-else-if="index === 8" alt="">
                </el-col>
              </el-row>
            </div>


            <div style="display: flex; flex-direction: column">
              <!--  话题   -->
              <div style="margin: 0 0 10px 0">
                <el-tag v-if="item.topics" style="margin-right: 5px" v-for="topic in item.topics" :key="topic">#
                  {{ topic }}
                </el-tag>
              </div>
              <!--  浏览量 时间  -->
              <div style="display: flex">
                <!--  浏览量 -->
                <div style="flex: 1; font-size: 16px; color: #888">
                  <!--  动态热度   -->
<!--                  <span style="">-->
<!--                    <el-icon :size="20" style="top: 4px">-->
<!--                      <img style="width: 20px" src="../../assets/svg/hot.svg" alt="">-->
<!--                    </el-icon>-->
<!--                    <span style="margin-left: 2px; color: red">{{ item.hot }}</span>-->
<!--                  </span>-->

                  <!--  浏览数量    -->
                  <span style="">
                    <el-icon :size="20" style="top: 4px"><View/></el-icon>
                    <span style="margin-left: 3px">{{ item.view }}</span>
                  </span>

                  <!--  点赞数量    -->
                  <span style="margin-left: 20px">
                    <el-icon :size="20" style="top: 4px"><img style="width: 20px" src="../../assets/svg/praise.svg" alt="">
                    </el-icon>
                    <span style="margin-left: 2px">{{ item.praiseCount }}</span>
                  </span>
                  <!--  收藏数量    -->
                  <span style="margin-left: 20px">
                    <el-icon :size="20" style="top: 4px"><Star/></el-icon>
                    <span style="margin-left: 3px">{{ item.collectCount }}</span>
                  </span>
                  <!--  评论数量    -->
                  <span style="margin-left: 20px">
                    <el-icon :size="20" style="top: 4px"><ChatLineSquare/></el-icon>
                    <span style="margin-left: 3px">{{ item.commentCount }}</span>
                  </span>



                </div>
                <!--  时间  -->
                <div style="text-align: right;">
                  <span style="font-size: 12px; color: #888">{{ item.time }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>

      </div>


      <div style="width: 40%; margin-left: 10px">
        <!--   校园公告   -->
        <div style="margin-left: 10px">
          <el-card>
            <div style="padding-bottom: 10px; border-bottom: 1px solid #ccc; ">
              <span style="font-size: 20px; font-weight: bold; color: orange">校园公告</span>
              <el-button link style="float: right; top: 5px; font-size: 14px; color: dodgerblue"
                         @click="router.push('/front/info')">
                查看更多
              </el-button>

            </div>
            <div v-for="(item, index) in state.infos" :key="item.id" style="margin: 10px 0">
              <div style="cursor: pointer" class="overflowShow" @click="router.push('/front/infoDetail?id=' + item.id)">
                <span style="color: orangered">{{ index + 1 }}</span>
                <span style="margin-left: 10px">{{ item.name }}</span>
              </div>
            </div>
          </el-card>
        </div>

        <!--   校园活动   -->
        <div style="margin: 20px 0 0 10px">
          <el-card>
            <div style="padding-bottom: 10px; border-bottom: 1px solid #ccc;">
              <span style="font-size: 20px; font-weight: bold; color: dodgerblue">校园活动</span>
              <el-button link style="float: right; top: 5px; font-size: 14px; color: dodgerblue"
                         @click="router.push('/front/activity')">
                查看更多
              </el-button>
            </div>
            <div v-for="(item, index) in state.activities" :key="item.id" style="margin: 10px 0">
              <div class="overflowShow">
                <span style="color: royalblue">{{ index + 1 }}</span>
                <span style="margin-left: 10px">{{ item.name }}</span>
              </div>
            </div>
          </el-card>
        </div>

        <!--   活跃用户   -->
        <div style="margin: 20px 0 0 10px">
          <el-card>
            <div style="padding-bottom: 10px; border-bottom: 1px solid #ccc;">
              <span style="font-size: 20px; font-weight: bold; color: limegreen">活跃用户</span>
              <el-button link style="float: right; top: 5px; font-size: 14px; color: dodgerblue"
                         @click="router.push('/front/user')">
                查看更多
              </el-button>
            </div>
            <div v-for="(item, index) in state.users" :key="item.id" style="margin: 10px 0">
              <div style="cursor: pointer" class="overflowShow" @click="router.push('/front/userDetail?id='+item.id)">
                <span style="color: forestgreen; margin-right: 10px">{{ index + 1 }}</span>
                <img :src="item.avatar" alt="" style="width: 30px; height: 30px; border-radius: 50%; top: 10px">
                <span style="margin-left: 10px">{{ item.name }}</span>
                <!--  活跃度（积分排行）   -->
                <span style="margin-left: 10px">
                <el-icon :size="20" style="top: 4px">
                  <img style="width: 20px" src="../../assets/imgs/trophy.png" alt="">
                </el-icon>
                <span style="margin-left: 2px; color: red">{{ item.score }}</span>
                </span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

    </div>


  </div>
</template>

<style scoped>
.refresh:hover {
  cursor: pointer;
}
</style>