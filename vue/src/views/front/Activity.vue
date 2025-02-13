<script setup>
import {nextTick, reactive, ref} from "vue";
import request from "@/utils/request";
import router from "@/router";
import {ElMessage} from "element-plus";
import config from "../../../config";
import {useUserStore} from "@/stores/user";
import {onBeforeUnmount, shallowRef} from "vue"
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'


const name = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const userStore = useUserStore()
const token = userStore.getBearerToken
const auths = userStore.getAuths

const state = reactive({
  tableData: [],
  form: {},
  statusOption: [
    {value: '未开始', label: '未开始'},
    {value: '进行中', label: '进行中'},
    {value: '已结束', label: '已结束'},
  ],


})

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


const load = () => {
  request.get('/activity/page', {
    params: {
      name: name.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.tableData = res.data.records
    total.value = res.data.total
  })
}
load()  // 调用 load方法拿到后台数据

const reset = () => {
  name.value = ''
  load()
}


// 预约
const handleReserve = (row) => {
  request.post('/reserve', {activityId: row.id}).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

</script>

<template>
  <div style="width: 80%; margin: auto">

    <el-card style="margin: 10px 0">
      <div style="display: flex; padding-bottom: 10px; border-bottom: 1px solid #ccc; margin-bottom: 10px">
        <div style="font-size: 20px; font-weight: bold; color: orange">
          校园活动
        </div>
        <div style="text-align: right; flex: 1; margin-right: 10px">
          <el-button type="primary" link @click="router.push('/front/myReserve')">
            <span style="font-size: 18px; top: 5px; font-weight: bold"> 查看我的预约 </span>
          </el-button>
        </div>
      </div>
      <el-table :data="state.tableData" stripe border>
        <el-table-column type="index" label="序号" width="60"/>
        <el-table-column prop="name" label="活动名称"></el-table-column>
        <el-table-column label="活动内容">
          <template #default="scope">
            <el-button @click="view(scope.row.content)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150px" ></el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="150px" ></el-table-column>
        <el-table-column prop="nums" label="总人数"></el-table-column>
        <el-table-column prop="leftNums" label="剩余可预约人数"></el-table-column>
        <el-table-column prop="location" label="活动地点"></el-table-column>
        <el-table-column prop="fee" label="活动经费"></el-table-column>
        <el-table-column label="活动图片" width="120px">
          <template #default="scope">
            <el-image preview-teleported style="width: 80px; height: 80px" :src="scope.row.img" v-if="scope.row.img"
                      :preview-src-list="[scope.row.img]"></el-image>
          </template>
        </el-table-column>
        <el-table-column label="活动附件" width="140px">
          <template #default="scope">
            <el-button type="primary" v-if="scope.row.file">
              <el-icon style="vertical-align: middle">
                <download/>
              </el-icon>
              <a :href="scope.row.file" target="_blank" style="text-decoration: none; color: white">
                <span style="vertical-align: middle; margin-left: 5px"> 点击下载 </span>
              </a>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="活动状态" width="90">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '未开始'">未开始</el-tag>
            <el-tag v-if="scope.row.status === '进行中'" type="success">进行中</el-tag>
            <el-tag v-if="scope.row.status === '已结束'" type="danger">已结束</el-tag>
          </template>
        </el-table-column>


        <el-table-column label="预约" width="90" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === '未开始' && !scope.row.hasReserve" type="primary" @click="handleReserve(scope.row)">预约</el-button>
            <el-tag v-else-if="scope.row.hasReserve" type="warning">已预约</el-tag>
          </template>
        </el-table-column>

      </el-table>
    </el-card>

    <!--    动态分页    -->
    <el-card style="margin: 10px 0; background-color: white">
      <div style="display: flex">
        <div>
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
    </el-card>

    <el-dialog v-model="viewShow" title="活动内容" width="40%">
      <div id="editor-content-view" class="editor-content-view" v-html="content" style="padding: 0 20px"></div>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="viewShow = false">关闭</el-button>
      </span>
      </template>
    </el-dialog>
  </div>
</template>