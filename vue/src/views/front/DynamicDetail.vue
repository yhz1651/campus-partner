<script setup>
import router from "@/router";
import request from "@/utils/request";
import {nextTick, onBeforeUnmount, onMounted, reactive, ref, shallowRef} from "vue";
import {ElMessage} from "element-plus";
import {useUserStore} from "@/stores/user";
import config from "../../../config";
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'

const userStore = useUserStore()
const user = userStore.getUser
const id = router.currentRoute.value.query.id // 参数id
const token = userStore.getBearerToken
const auths = userStore.getAuths

const state = reactive({
  dynamic: {},
  form: {},
  comments: [],
  parent: {}
})

const load = () => {
  request.get('/dynamic/' + id).then(res => {
    state.dynamic = res.data
  })

  request.get('/comment/tree?dynamicId=' + id).then(res => {
    state.comments = res.data
  })
}
onMounted(() => {
  load()
})

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

const ruleFormRef = ref()
const rules = reactive({
  name: [
    {required: true, message: '请输入标题', trigger: 'blur'},
  ]
})
const dialogFormVisible = ref(false)
const dialogEditFormVisible = ref(false)
const handleComment = (row, puser) => {
  dialogFormVisible.value = true
  state.form = {}
  if (row && row.id) {  // row是父节点的数据，如果存在，则为评论
    state.parent = row
    if (puser) {
      state.parent.user = puser
    }
  }
}

const del = (id) => {
  request.delete('/comment/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success("删除成功")
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 发送评论数据给后台
const save = () => {
  if (!state.form.content) {
    ElMessage.error("评论不能为空")
    return
  }
  if (state.parent.id) {  // 回复的时候触发
    state.form.pid = state.parent.id
    state.form.puserId = state.parent.user.id  // 传父级评论的用户id
  }
  state.form.dynamicId = state.dynamic.id  // 当前模块的id
  // 发送数据
  request.post('/comment', state.form).then(res => {
    if (res.code === '200') {
      ElMessage.success("评论成功，积分+3")
      dialogFormVisible.value = false
      load()
      state.parent = {}
      state.form.content = ''
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const praise = () => {
  request.post('/praise', {fid: id, type: 'dynamic'}).then(res => {
    ElMessage.success('点赞成功，积分+1')
    load()
  })
}
const praiseCancel = () => {
  request.post('/praise', {fid: id, type: 'dynamic'}).then(res => {
    ElMessage.success('取消点赞成功')
    load()
  })
}
const praiseComment = (commentId) => {
  request.post('/praise', {fid: commentId, type: 'comment'}).then(res => {
    ElMessage.success('点赞成功，积分+1')
    load()
  })
}
const praiseCommentCancel = (commentId) => {
  request.post('/praise', {fid: commentId, type: 'comment'}).then(res => {
    ElMessage.success('取消点赞成功')
    load()
  })
}
const collect = () => {
  request.post('/collect', {dynamicId: id}).then(res => {
    ElMessage.success('收藏成功，积分+2')
    load()
  })
}
const collectCancel = () => {
  request.post('/collect', {dynamicId: id}).then(res => {
    ElMessage.success('取消收藏成功')
    load()
  })
}

const imgs = ref([])  // 新增

// 图片上传回调函数
const handleImgUploadSuccess = (res) => {
  imgs.value.push({ name: res.data.substring(res.data.lastIndexOf('/') + 1), url: res.data })
}

// 编辑动态
const handleEdit = (raw) => {
  dialogEditFormVisible.value = true
  nextTick(() => {
    ruleFormRef.value.resetFields()
    state.form = JSON.parse(JSON.stringify(raw))
    valueHtml.value = raw.content  // 富文本
    imgs.value = raw.imgs || []   // 编辑
  })
}

// 保存动态
const saveEdit = () => {
  ruleFormRef.value.validate(valid => {   // valid就是校验的结果
    if (valid) {
      state.form.content = valueHtml.value  // 富文本保存内容
      state.form.imgs = imgs.value
      request.request({
        url: '/dynamic',
        method: state.form.id ? 'put' : 'post',
        data: state.form
      }).then(res => {
        if (res.code === '200') {
          ElMessage.success('保存成功')
          dialogEditFormVisible.value = false
          state.form.content = ''
          load()  // 刷新表格数据
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

// 删除动态
const delDynamic = (id) => {
  request.delete('/dynamic/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      router.push('/front/MyDynamic')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

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
  <div style="width: 60%; margin: auto">
    <el-card v-if="state.dynamic">

      <div
          style="padding-bottom: 15px; border-bottom: 1px solid #ddd; margin-bottom: 20px; display: flex; flex-direction: column">

        <!-- 标题、修改、删除  -->
        <div style="display: flex; flex-direction: column; margin-left: 10px" v-if="state.dynamic">
          <!-- 动态标题  -->
          <span style="font-weight: bold; font-size: 30px">{{ state.dynamic.name }}</span>

          <!-- 浏览数量  -->
          <div>
            <el-icon style="top: 2px">
              <View/>
            </el-icon>
            <span style="margin-left: 5px">{{ state.dynamic.view }}</span>

            <!--  评论数量    -->
            <el-icon style="top: 2px; margin-left: 20px">
              <ChatLineSquare/>
            </el-icon>
            <span style="margin-left: 5px">{{ state.dynamic.commentCount }}</span>

            <!-- 时间  -->
            <el-icon style="top: 2px; margin-left: 20px">
              <Clock/>
            </el-icon>
            <span style="margin-left: 5px">{{ state.dynamic.time }}</span>

            <!-- 删除 回复 -->
            <el-button style="margin-left: 20px;" type="primary" link v-if="state.dynamic.userId === user.id"
                       @click="handleEdit(state.dynamic)">修改
            </el-button>
            <el-popconfirm title="您确定删除吗？" @confirm="delDynamic(state.dynamic.id)">
              <template #reference>
                <el-button type="danger" link style="color: red;" v-if="state.dynamic.userId === user.id">删除
                </el-button>
              </template>
            </el-popconfirm>


          </div>

        </div>

        <!-- 发表人头像、名字、关注  -->
        <div style="display: flex; margin: 20px 0 10px 10px;" v-if="state.dynamic.user">
          <!-- 头像 -->
          <img :src="state.dynamic.user.avatar" alt=""
               style="width: 40px; height: 40px; border-radius: 50%; margin-top: 5px; cursor: pointer"
               @click="router.push('/front/userDetail?id=' + state.dynamic.user.id)">

          <!-- 姓名 简介 -->
          <div style="display: flex; flex: 1; flex-direction: column; cursor: pointer"
               @click="router.push('/front/userDetail?id=' + state.dynamic.user.id)">
            <div style="display: flex;">
              <!--   名字   -->
              <div style=" margin-left: 10px;  text-align: left" v-if="state.dynamic.user">
                <span style="font-weight: bold; font-size: 16px">{{ state.dynamic.user.name }}</span>
              </div>
              <!--  积分等级  -->
              <div style="text-align: left" v-if="state.dynamic.user">
                <span style="margin-left: 5px">
                  <el-icon :size="20" style="top: 4px">
                    <img style="width: 20px" src="../../assets/imgs/trophy.png" alt="">
                  </el-icon>
                  <span style="color: orange">{{ state.dynamic.user.score }}</span>
                </span>
              </div>
              <!--  关注 粉丝牌  -->
              <div style="margin-left: 5px" v-if="state.dynamic.user.hasFollow">
                <el-tag type="primary">我的关注</el-tag>
              </div>
              <div style="margin-left: 5px" v-if="state.dynamic.user.hasBeFollow">
                <el-tag type="danger">我的粉丝</el-tag>
              </div>

            </div>

            <div style="display: flex; color: #888; margin-left: 10px" v-if="state.dynamic.user">
              {{ state.dynamic.user.profile }}
            </div>

          </div>

          <!-- 关注  -->
          <div style="display: flex; margin-right: 20px; align-items: center; "
               v-if="state.dynamic.user.id !== user.id">
            <!-- 互相关注  -->
            <div style="display: flex" v-if="state.dynamic.user.hasFollow && state.dynamic.user.hasBeFollow">
              <el-popconfirm title="您确定取消关注吗？" @confirm="followCancel(state.dynamic.user.id)">
                <template #reference>
                  <el-button type="success" style="font-size: 16px;">互相关注</el-button>
                </template>
              </el-popconfirm>
            </div>

            <!-- 已关注  -->
            <div style="display: flex" v-else-if="state.dynamic.user.hasFollow">
              <el-popconfirm title="您确定取消关注吗？" @confirm="followCancel(state.dynamic.user.id)">
                <template #reference>
                  <el-button type="primary" style="font-size: 16px;">已关注</el-button>
                </template>
              </el-popconfirm>
            </div>

            <!--  未关注  -->
            <div v-else>
              <el-button type="danger" style="font-size: 16px;" @click="follow(state.dynamic.user.id)">关注</el-button>
            </div>

          </div>
        </div>


      </div>


      <!--   内容   -->
      <div style="display: flex; align-items: flex-end">
        <div style="flex: 1" class="editor-content-view" v-html="state.dynamic.content"></div>
      </div>

      <!--   图片   -->
      <div style="margin-top: 20px">
        <el-row :gutter="10">
          <el-col :span="8" style="margin-bottom: 10px" v-for="(img,index) in state.dynamic.imgs" :key="img">
            <el-image preview-teleported :preview-src-list="[img.url]" style="width: 100%; height: 350px" :src="img.url"></el-image>
          </el-col>
        </el-row>
      </div>

      <!--   话题   -->
      <div style="margin: 20px 0 5px 10px">
        <el-tag size="large" style="margin-right: 10px; font-size: 16px" v-for="topic in state.dynamic.topics"
                :key="topic">
          # {{ topic }}
        </el-tag>
      </div>
    </el-card>


    <!-- 点赞  收藏  发表评论-->
    <el-card style="margin: 10px 0" v-if="state.dynamic">
      <div style="text-align: left; margin: 0 0 30px 10px">
        <!-- 点赞 -->
        <span class="icon" v-if="state.dynamic.hasPraise" @click="praiseCancel">
              <el-icon style="top: 5px; margin-left: 10px"><img src="../../assets/svg/praise-active.svg" alt=""
                                                                style="width: 30px;"></el-icon>
              <span style="margin-left: 20px; color: red; font-size: 30px">{{ state.dynamic.praiseCount }}</span>
          </span>
        <span class="icon" v-else @click="praise">
              <el-icon style="top: 5px; margin-left: 10px"><img src="../../assets/svg/praise.svg" alt=""
                                                                style="width: 30px;"></el-icon>
              <span style="margin-left: 20px; color: #888; font-size: 30px">{{ state.dynamic.praiseCount }}</span>
          </span>
        <!-- 收藏  -->
        <span class="icon" style="margin-left: 30px" v-if="state.dynamic.hasCollect" @click="collectCancel">
              <el-icon style="top: 5px; margin-left: 10px;"><img src="../../assets/svg/collect-active.svg" alt=""
                                                                 style="width: 30px;"></el-icon>
              <span style="margin-left: 20px; color: red; font-size: 30px">{{ state.dynamic.collectCount }}</span>
          </span>
        <span class="icon" style="margin-left: 30px" v-else @click="collect">
              <el-icon style="top: 5px; margin-left: 10px"><img src="../../assets/svg/collect.svg" alt=""
                                                                style="width: 30px;"></el-icon>
              <span style="margin-left: 20px; color: #888; font-size: 30px">{{ state.dynamic.collectCount }}</span>
          </span>
      </div>
      <div style="display: flex; align-items: center">
        <!-- 发表评论 -->
        <el-input type="textarea" style="flex: 1" v-model=state.form.content placeholder="请输入您的评论"></el-input>
        <div style="width: 80px; text-align: right;">
          <el-button type="primary" @click="save">
            发布
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 评论区 -->
    <el-card style="margin: 10px 0">
      <div style="padding-bottom: 10px; border-bottom: 1px solid #ddd">评论列表</div>

      <div v-if="state.comments && state.comments.length">
        <div v-for="item in state.comments" :key="item.id" style="padding: 10px 20px;">
          <div style="display: flex; align-items: center; margin-top: 5px">
            <!-- 头像 -->
            <img v-if="item.user" style="width: 40px; height: 40px; border-radius: 50%" :src="item.user.avatar" alt="">
            <!-- 用户名 -->
            <span v-if="item.user" style="margin-left: 10px; font-weight: bold">{{ item.user.name }}</span>
          </div>

          <!-- 评论内容 -->
          <div style="line-height: 30px; padding-left: 50px; color: black; margin: 5px 0">{{ item.content }}</div>

          <div style="padding-left: 50px; font-size: 12px; color: #999; display: flex">
            <div>
              <!-- 时间 -->
              <el-icon style="top: 2px">
                <Clock/>
              </el-icon>
              <span>{{ item.time }}</span>

              <!-- IP地址 -->
              <el-icon style="top: 2px; margin-left: 10px">
                <Location/>
              </el-icon>
              <span>{{ item.location }}</span>

              <!-- 评论点赞 -->
              <span v-if="item.hasPraise" @click="praiseCommentCancel(item.id)" class="icon">
                <el-icon style="top: 2px; margin-left: 10px">
                  <img src="../../assets/svg/praise-active.svg" alt="" style="width: 16px;">
                </el-icon>
                <span style="margin-left: 5px; color: red; font-size: 16px">{{ item.praiseCount }}</span>
              </span>
              <span class="icon" v-else @click="praiseComment(item.id)">
                <el-icon style="top: 2px; margin-left: 10px">
                  <img src="../../assets/svg/praise.svg" alt="" style="width: 16px;">
                </el-icon>
                <span style="margin-left: 5px; color: #888888; font-size: 16px">{{ item.praiseCount }}</span>
              </span>
            </div>

            <!-- 删除 回复 -->
            <div style="text-align: left; margin-left: 10px; line-height: 16px; top: 4px">
              <el-button type="primary" link @click="handleComment(item, null)">回复</el-button>
              <el-popconfirm title="您确定删除吗？" @confirm="del(item.id)">
                <template #reference>
                  <el-button type="danger" link style="color: red;" v-if="item.userId === user.id">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>

          <div style="margin-top: 20px; margin-left: 50px; border-bottom: 1px solid #ddd;">
            <!-- 回复的子节点 -->
            <div v-for="subItem in item.children" :key="subItem.id" style="padding: 10px 0">
              <div style="display: flex; align-items: center">
                <img v-if="subItem.user" style="width: 40px; height: 40px; border-radius: 50%"
                     :src="subItem.user.avatar" alt="">
                <span v-if="subItem.user" style="margin-left: 10px; font-weight: bold">{{ subItem.user.name }}</span>
              </div>

              <div style="line-height: 30px; padding-left: 50px; color: #666; margin: 5px 0">
                <span style="color: #888">回复<span style="color: orange" v-if="subItem.puser">@{{
                    subItem.puser.name
                  }}</span>：</span>
                <span style="color: black">{{ subItem.content }}</span>
              </div>

              <div style="padding-left: 50px; font-size: 12px; color: #999; display: flex">
                <div>
                  <el-icon style="top: 2px">
                    <Clock/>
                  </el-icon>
                  <span>{{ subItem.time }}</span>

                  <el-icon style="top: 2px; margin-left: 10px">
                    <Location/>
                  </el-icon>
                  <span>{{ subItem.location }}</span>


                  <!-- 评论点赞 -->
                  <span v-if="subItem.hasPraise" @click="praiseCommentCancel(subItem.id)" class="icon">
                    <el-icon style="top: 2px; margin-left: 10px">
                      <img src="../../assets/svg/praise-active.svg" alt="" style="width: 16px;">
                    </el-icon>
                    <span style="margin-left: 5px; color: red; font-size: 16px">{{ subItem.praiseCount }}</span>
                  </span>
                  <span class="icon" v-else @click="praiseComment(subItem.id)">
                    <el-icon style="top: 2px; margin-left: 10px">
                      <img src="../../assets/svg/praise.svg" alt="" style="width: 16px;">
                    </el-icon>
                    <span style="margin-left: 5px; color: #888888; font-size: 16px">{{ subItem.praiseCount }}</span>
                  </span>
                </div>

                <div style="text-align: left; margin-left: 10px; line-height: 16px; top: 4px">
                  <el-button type="primary" link @click="handleComment(item, subItem.user)">回复</el-button>
                  <el-popconfirm title="您确定删除吗？" @confirm="del(subItem.id)">
                    <template #reference>
                      <el-button type="danger" link style="color: red;" v-if="subItem.userId === user.id">删除
                      </el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
            </div>

          </div>
        </div>
      </div>

      <div v-else style="text-align: center; margin: 10px 0; ">
        <img src="../../assets/imgs/nocomment.png" alt="" style="width: 300px">
        <div style="font-size: 26px">暂无评论</div>
        <div><span style="color: #999">快来评论一下吧~</span></div>
      </div>
    </el-card>

    <el-dialog v-model="dialogFormVisible" title="评论" width="50%">
      <el-form :model="state.form" style="padding: 0 20px" status-icon>
        <el-form-item>
          <el-input type="textarea" placeholder="请输入您的评论" v-model="state.form.content"
                    autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="save">
          确定
        </el-button>
      </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogEditFormVisible" title="动态信息" width="60%">
      <el-form ref="ruleFormRef" :rules="rules" :model="state.form" label-width="80px" style="padding: 0 20px"
               status-icon>
        <el-form-item prop="name" label="标题">
          <el-input v-model="state.form.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="content" label="内容">
          <div style="border: 1px solid #ccc">
            <Toolbar
                style="border-bottom: 1px solid #ccc"
                :editor="editorRef"
                :mode="'simple'"
            />
            <Editor
                style="height: 300px; overflow-y: hidden;"
                v-model="valueHtml"
                :defaultConfig="editorConfig"
                :mode="'simple'"
                @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
        <el-form-item prop="topics" label="话题">
          <el-select v-model="state.form.topics" style="width: 50%" multiple filterable>
            <el-option v-for="item in state.topics" :label="item.name" :key="item.id" :value="item.name"></el-option>
          </el-select>
        </el-form-item>
<!--        <el-form-item prop="img" label="封面">-->
<!--          <el-upload :show-file-list="false" :action="`http://${config.serverUrl}/file/upload`" ref="file"-->
<!--                     :headers="{ Authorization: token}" :on-success="handleImgUploadSuccess">-->
<!--            <el-button size="small" type="primary">点击上传</el-button>-->
<!--          </el-upload>-->
<!--        </el-form-item>-->
        <el-form-item prop="imgs" label="图片">
          <el-upload
              :action='`http://${config.serverUrl}/file/upload`'
              :on-success="handleImgUploadSuccess"
              :headers="{ Authorization: token}"
              multiple
              :file-list="imgs"
              list-type="picture"
          >
            <el-button type="primary">
              <el-icon style="vertical-align: middle">
                <Top />
              </el-icon>  <span style="vertical-align: middle"> 上传 </span>
            </el-button>
          </el-upload>
        </el-form-item>

      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogEditFormVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">
          保存
        </el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<style>
.icon:hover {
  cursor: pointer;
}
</style>