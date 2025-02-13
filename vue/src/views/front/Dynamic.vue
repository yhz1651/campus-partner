<script setup>
import {reactive, ref} from "vue";
import router from "@/router"
import request from "@/utils/request";
import {ElMessage} from "element-plus";


const state = reactive({
  hotDynamics: [],
  dynamics: [],
  infos: [],
  topics: []
})

const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
let name = ref('')


const load = () => {
  request.get('/dynamic/page', {
    params: {
      name: name.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.dynamics = res.data.records
    total.value = res.data.total
  })
  request.get('/dynamic/hot').then(res => {
    state.hotDynamics = res.data
  })
  request.get('/topic/hot').then(res => {
    state.topics = res.data
  })
}
load()  // 调用 load方法拿到后台数据

// 按照话题加载
const handleTopic = (topic) => {
  request.get('/dynamic/page', {
    params: {
      name: topic,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.dynamics = res.data.records
    total.value = res.data.total
  })

}

</script>

<template>
  <div style="width: 60%; margin: auto;">

    <div style="display: flex;">
      <!--    动态    -->
      <div style="width: 60%; color: #666">
        <!--    动态列表    -->
        <el-card>
          <div style="padding-bottom: 10px; border-bottom: 1px solid #ccc; display: flex">
            <div style="flex: 1; font-size: 20px; font-weight: bold; color: orange; ">全部动态</div>
            <div style="margin-right: 10px">
              <el-input v-model="name" placeholder="请输入关键词" style="width: 200px" clearable/>
              <el-button type="primary" class="ml5" @click="load">
                <el-icon style="vertical-align: middle">
                  <Search/>
                </el-icon>
                <span style="vertical-align: middle"> 搜索 </span>
              </el-button>

            </div>
          </div>

          <div v-for="item in state.dynamics" :key="item.id"
               style="border-bottom: 1px solid #ddd; padding: 20px; cursor: pointer;"
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
                  <!--  浏览数量    -->
                  <span>
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

        <!--    动态分页    -->
        <el-card style="margin: 10px 0; background-color: white">
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


      <div style="width: 40%; margin-left: 20px; display: flex; flex-direction: column">
        <!--    热门动态    -->
        <div style="margin-bottom: 20px">
          <el-card style="max-height: 500px;">
            <div style="padding-bottom: 10px; border-bottom: 1px solid #ccc; font-size: 20px; font-weight: bold; color: orangered">
              热门动态
            </div>
            <div v-for="(item, index) in state.hotDynamics" :key="item.id" style="margin: 10px 0; cursor: pointer" @click="router.push('/front/dynamicDetail?id=' + item.id)">
              <div class="overflowShow">
                <!--  排名   -->
                <span style="color: deeppink">{{ index + 1 }}</span>
                <!--  动态标题   -->
                <span style="margin-left: 10px">{{ item.name }}</span>
                <!--  动态热度   -->
                <span style="margin-left: 10px">
                <el-icon :size="20" style="top: 4px">
                  <img style="width: 20px" src="../../assets/svg/hot.svg" alt="">
                </el-icon>
                <span style="margin-left: 2px; color: red">{{ item.hot }}</span>
              </span>

              </div>
            </div>
          </el-card>
        </div>

        <!--    热门话题    -->
        <div>
          <el-card style="max-height: 500px;">
            <div style="margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #ccc; font-size: 20px; font-weight: bold; color: blueviolet">
              热门话题
            </div>

            <el-row :gutter="10" style="margin: 5px 0">
              <el-col :span="6" style="" v-for="item in state.topics" :key="item.id">
                <el-button style="margin-bottom: 20px;  border-radius: 10%; cursor: pointer; height: 60px; border: 1px solid #ccc;" @click="handleTopic(item.name)">
                  <div style="padding: 5px; text-align: center; font-size: 16px">{{ item.name }}</div>
                </el-button>
              </el-col>
            </el-row>


          </el-card>
        </div>
      </div>


    </div>


  </div>
</template>

