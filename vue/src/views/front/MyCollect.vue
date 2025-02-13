<script setup>
import router from "@/router";
import request from "@/utils/request";
import {nextTick, onBeforeUnmount, reactive, ref, shallowRef} from "vue";
import config from "../../../config";
import {useUserStore} from "@/stores/user";
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {ElMessage} from "element-plus";

const userStore = useUserStore()
let user = userStore.getUser
const activePath = router.currentRoute.value.path
const token = userStore.getBearerToken
const auths = userStore.getAuths

const state = reactive({
  dynamics: [],
  form: {}
})

const pageNum = ref(1)
const pageSize = ref(5)
const total = ref(0)
const load = () => {
  request.get('/dynamic/page', {
    params: {
      type: 'collect',
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    if (res.data) {
      state.dynamics = res.data.records
      total.value = res.data.total
    }
  })
}
load()  // 调用 load方法拿到后台数据

state.topics = []
request.get('/topic').then(res => state.topics = res.data)

const valueHtml = ref('')  // 富文本内容

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {
    uploadImage: {
      fieldName: 'file',
      server: `http://${config.serverUrl}/file/uploadImg`,
      headers: {
        Authorization: token
      }
    }
  }
}
const content = ref('')
const viewShow = ref(false)
const view = (value) => {
  viewShow.value = true
  content.value = value
}
const handleCreated = (editor) => {
  editorRef.value = editor // 记录 editor 实例，重要！
}
// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})


const dialogFormVisible = ref(false)
const ruleFormRef = ref()
const rules = reactive({
  name: [
    {required: true, message: '请输入标题', trigger: 'blur'},
  ]
})

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


      <!--    动态列表    -->
      <el-card style="min-height: calc(100vh - 100px)">
        <div style="display: flex; padding-bottom: 10px; border-bottom: 1px solid #ccc;">
          <div style="font-size: 20px; font-weight: bold; color: orange">
            我的收藏
          </div>
        </div>

        <!--    有数据    -->
        <div v-if="total" v-for="item in state.dynamics" :key="item.id" style="border-bottom: 1px solid #ddd; padding: 20px; cursor: pointer;" @click="router.push('/front/dynamicDetail?id=' + item.id)">

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
                <el-image style="width: 100%; height: 270px" :src="img.url" v-if="index < 8"></el-image>
                <img style="width: 100%; height: 270px" src="../../assets/imgs/ellipsis.png" v-else-if="index === 8" alt="">
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

        <!--    无数据    -->
        <div v-else style="text-align: center; margin: 10px 0;">
          <img src="../../assets/imgs/nodynamic.png" alt="" style="width: 300px">
          <div style="font-size: 26px">暂无数据</div>
          <div style="cursor: pointer; color: dodgerblue" @click="router.push('/front/dynamic')">快去收藏一个动态吧~</div>
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