<script setup>
import router from "@/router";
import request from "@/utils/request";
import {useUserStore} from "@/stores/user";
import {nextTick, onBeforeUnmount, onMounted, reactive, ref, shallowRef} from "vue";
import config from "../../../config";
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {ElMessage} from "element-plus";


const userStore = useUserStore()
const user = userStore.getUser

const id = router.currentRoute.value.query.id // 参数id 用户id
const state = reactive({
  users: {},
  dynamics: [],
})


const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const typeId = ref(0)

const load = () => {
  request.get('/user/' + id).then(res => {
    state.users = res.data
  })
  request.get('/dynamic/page', {
    params: {
      type: 'user',
      typeId: id,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.dynamics = res.data.records
    total.value = res.data.total
  })
}
onMounted(() => {
  load()
})

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
  <div style="width: 60%; margin: auto;">

    <el-card v-if="state.users" style="min-height: calc(100vh - 100px)">

      <!-- 发表人头像、名字、关注  -->
      <div style="display: flex; margin: 10px 0 10px 10px;" v-if="state.users">
        <!-- 头像 -->
        <img :src="state.users.avatar" alt="" style="width: 150px; height: 150px; border-radius: 50%; margin-top: 5px">

        <!-- 姓名 简介 粉丝-->
        <div style="display: flex; flex: 1; flex-direction: column; margin-left: 10px">
          <!-- 第一行 姓名 性别 等级-->
          <div style="display: flex;">
            <!--   名字   -->
            <div style=" margin-left: 10px;  text-align: left" v-if="state.users">
              <span style="font-weight: bold; font-size: 30px">{{ state.users.name }}</span>
            </div>

            <!--  积分等级  -->
            <div style="text-align: left" v-if="state.users">
              <span style="margin-left: 20px">
                <el-icon style="top: 15px">
                  <img style="width: 40px" src="../../assets/imgs/trophy.png" alt="">
                </el-icon>
                <span style="color: orange; font-size: 26px; margin-left: 10px; top: 5px;">{{ state.users.score }}</span>
              </span>
            </div>

            <!--  关注 粉丝牌  -->
            <div style="margin-left: 5px; line-height: 50px;" v-if="state.users.hasFollow && state.users.id !== user.id">
              <el-tag type="primary">我的关注</el-tag>
            </div>
            <div style="margin-left: 5px; line-height: 50px;" v-if="state.users.hasBeFollow && state.users.id !== user.id">
              <el-tag type="danger">我的粉丝</el-tag>
            </div>
          </div>

          <!-- 第二行 简介 -->
          <div style="display: flex; color: #555; margin-left: 10px; font-size: 16px">
            {{ state.users.profile }}
          </div>

          <!-- 第三行 年龄 学校 -->
          <div style="display: flex; margin: 10px 0 0 10px" >
            <el-tag v-if="state.users.sex">
              <img v-if="state.users.sex === '男'" src="../../assets/imgs/male.png" style="width: 26px">
              <img v-if="state.users.sex === '女'" src="../../assets/imgs/female.png" style="width: 26px">
            </el-tag>

            <el-tag v-if="state.users.age" type="primary" style="margin-left: 5px">{{ state.users.age }} 岁</el-tag>
            <el-tag v-if="state.users.school" type="primary" style="margin-left: 5px">{{ state.users.school }}</el-tag>
            <el-tag v-if="state.users.address" type="primary" style="margin-left: 5px">{{ state.users.address }}</el-tag>
            <el-tag v-if="state.users.location" type="primary" style="margin-left: 5px">IP: {{ state.users.location }}</el-tag>
          </div>

          <!-- 第四行 粉丝 关注 -->
          <div style="display: flex; margin: 10px 0 0 10px">
            <span style="font-size: 30px; font-weight: bold">{{ state.users.bePraiseCount }}</span>
            <span style="font-size: 16px; color: #555555; margin-left: 5px; top: 12px">获赞</span>

            <span style="font-size: 30px; font-weight: bold; margin-left: 20px">{{ state.users.followCount }}</span>
            <span style="font-size: 16px; color: #555555; margin-left: 5px; top: 12px">关注</span>

            <span style="font-size: 30px; font-weight: bold; margin-left: 20px">{{ state.users.beFollowCount }}</span>
            <span style="font-size: 16px; color: #555555; margin-left: 5px; top: 12px">粉丝</span>

          </div>
        </div>

        <!-- 私信  -->
        <div style="display: flex; margin-right: 10px; align-items: center;" v-if="state.users.id !== user.id && state.users.hasFollow && state.users.hasBeFollow">
          <!-- 互相关注  -->
          <div style="display: flex">
            <el-button type="warning" style="font-size: 20px;">私信</el-button>
          </div>
        </div>

        <!-- 关注  -->
        <div style="display: flex; margin-right: 30px; align-items: center; " v-if="state.users.id !== user.id">
          <!-- 互相关注  -->
          <div style="display: flex" v-if="state.users.hasFollow && state.users.hasBeFollow">
            <el-popconfirm title="您确定取消关注吗？" @confirm="followCancel(state.users.id)">
              <template #reference>
                <el-button type="success" style="font-size: 20px;">互相关注</el-button>
              </template>
            </el-popconfirm>
          </div>

          <!-- 已关注  -->
          <div style="display: flex" v-else-if="state.users.hasFollow">
            <el-popconfirm title="您确定取消关注吗？" @confirm="followCancel(state.users.id)">
              <template #reference>
                <el-button type="primary" style="font-size: 20px;">已关注</el-button>
              </template>
            </el-popconfirm>
          </div>

          <!--  未关注  -->
          <div v-else>
            <el-button type="danger" style="font-size: 20px;" @click="follow(state.users.id)">关注</el-button>
          </div>

        </div>


        <div v-else style="align-items: center; margin: 10px 30px 0 0">
          <div>
            <el-button link type="primary" style="font-size: 16px;" @click="router.push('/front/person')">编辑个人信息</el-button>
          </div>
        </div>
      </div>

      <div style="font-size: 20px; font-weight: bold; color: orange; padding-bottom: 10px; border-bottom: 1px solid #ccc; margin: 20px 20px 0 20px">
        个人动态
      </div>

      <!--    有数据    -->
      <div v-if="total">
        <div v-for="item in state.dynamics" :key="item.id" style="border-bottom: 1px solid #ddd; padding: 20px; cursor: pointer;" @click="router.push('/front/dynamicDetail?id=' + item.id)">
          <div>
            <div style="display: flex">
              <!--   头像   -->
              <img v-if="item.user" :src="item.user.avatar" alt=""
                   style="width: 40px; height: 40px; border-radius: 50%">
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
                    <span style="color: orange;">{{ item.user.score }}</span>
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
          </div>
          <!--  动态标题和热度    -->
          <div style="margin: 10px 0; display: flex;">
            <!--  动态标题    -->
            <div style="color: #000; font-size: 18px; font-weight: bold">{{ item.name }}</div>
          </div>

          <!--  动态图片  -->
          <div style="margin: 10px 0" v-if="item.img">
            <div><img :src="item.img " alt="" style="height: 150px; width: 250px; border-radius: 10px"></div>
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
                  <el-icon :size="20" style="top: 4px"><img style="width: 20px" src="../../assets/svg/praise.svg"
                                                            alt="">
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
        <div style="margin: 20px 0 0 10px">
          <el-pagination
              @current-change="load"
              @size-change="load"
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              background
              layout="total, prev, pager, next"
              :total="total"
          />
        </div>
      </div>

      <!--    无数据    -->
      <div v-else style="text-align: center; margin: 10px 0;">
        <img src="../../assets/imgs/nodynamic.png" alt="" style="width: 300px">
        <div style="font-size: 26px">用户未发表动态</div>
      </div>

    </el-card>
  </div>
</template>