<script setup>
import {nextTick, reactive, ref} from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import config from "../../config";
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
  form: {}
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


state.userOptions = []
request.get('/user').then(res => state.userOptions = res.data)
state.noticeOptions = []
request.get('/notice').then(res => state.noticeOptions = res.data)
state.topics = []
request.get('/topic').then(res => state.topics = res.data)

const multipleSelection = ref([])

// 批量删除
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

const confirmDelBatch = () => {
  if (!multipleSelection.value || !multipleSelection.value.length) {
    ElMessage.warning("请选择数据")
    return
  }
  const idArr = multipleSelection.value.map(v => v.id)
  request.post('/dynamic/del/batch', idArr).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const imgs = ref([])  // 新增


// 图片上传回调函数
const handleImgUploadSuccess = (res) => {
  imgs.value.push({ name: res.data.substring(res.data.lastIndexOf('/') + 1), url: res.data })
}

const load = () => {
  request.get('/dynamic/page', {
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

const dialogFormVisible = ref(false)

const rules = reactive({
  name: [
    {required: true, message: '请输入标题', trigger: 'blur'},
    { min: 2, max: 20, message: '长度在2-20之间', trigger: 'blur' },
  ],
  content: [
    { max: 500, message: '长度不能大于500', trigger: 'blur' },
  ],
})
const ruleFormRef = ref()



// 新增
const handleAdd = () => {
  dialogFormVisible.value = true
  nextTick(() => {
    ruleFormRef.value.resetFields()
    state.form = {}
    valueHtml.value = ''  // 富文本
    imgs.value = []
  })
}

// 保存
const save = () => {
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
          dialogFormVisible.value = false
          load()  // 刷新表格数据
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

// 编辑
const handleEdit = (raw) => {
  dialogFormVisible.value = true
  nextTick(() => {
    ruleFormRef.value.resetFields()
    state.form = JSON.parse(JSON.stringify(raw))
    valueHtml.value = raw.content  // 富文本
    imgs.value = raw.imgs || []   // 编辑
  })
}

// 删除
const del = (id) => {
  request.delete('/dynamic/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 导出接口
const exportData = () => {
  window.open(`http://${config.serverUrl}/dynamic/export`)
}


const handleImportSuccess = () => {
  // 刷新表格
  load()
  ElMessage.success("导入成功")
}

const handleFileUploadSuccess = (res) => {
  state.form.file = res.data
  ElMessage.success('上传成功')
}

</script>

<template>
  <div>
    <div>
      <el-input v-model="name" placeholder="请输入标题" class="w300"/>
      <el-button type="primary" class="ml5" @click="load">
        <el-icon style="vertical-align: middle">
          <Search/>
        </el-icon>
        <span style="vertical-align: middle"> 搜索 </span>
      </el-button>
      <el-button type="warning" class="ml5" @click="reset">
        <el-icon style="vertical-align: middle">
          <RefreshLeft/>
        </el-icon>
        <span style="vertical-align: middle"> 重置 </span>
      </el-button>

    </div>

    <div style="margin: 10px 0">
<!--      <el-button type="success" @click="handleAdd" v-if="auths.includes('dynamic.add')">-->
<!--        <el-icon style="vertical-align: middle">-->
<!--          <Plus/>-->
<!--        </el-icon>-->
<!--        <span style="vertical-align: middle"> 新增 </span>-->
<!--      </el-button>-->
      <el-upload
          v-if="auths.includes('dynamic.import')"
          class="ml5"
          :show-file-list="false"
          style="display: inline-block; position: relative; top: 3px"
          :action='`http://${config.serverUrl}/dynamic/import`'
          :on-success="handleImportSuccess"
          :headers="{ Authorization: token}"
      >
        <el-button type="primary">
          <el-icon style="vertical-align: middle">
            <Bottom/>
          </el-icon>
          <span style="vertical-align: middle"> 导入 </span>
        </el-button>
      </el-upload>
      <el-button type="primary" @click="exportData" class="ml5" v-if="auths.includes('dynamic.export')">
        <el-icon style="vertical-align: middle">
          <Top/>
        </el-icon>
        <span style="vertical-align: middle"> 导出 </span>
      </el-button>
      <el-popconfirm title="您确定删除吗？" @confirm="confirmDelBatch" v-if="auths.includes('dynamic.deleteBatch')">
        <template #reference>
          <el-button type="danger" style="margin-left: 5px">
            <el-icon style="vertical-align: middle">
              <Remove/>
            </el-icon>
            <span style="vertical-align: middle"> 批量删除 </span>
          </el-button>
        </template>
      </el-popconfirm>
    </div>

    <div style="margin: 10px 0">
      <el-table :data="state.tableData" stripe border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column type="index" label="编号" width="60"></el-table-column>
        <el-table-column prop="name" label="标题"></el-table-column>
        <el-table-column label="内容">
          <template #default="scope">
            <el-button @click="view(scope.row.content)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="话题" width="150px">
          <template #default="scope">
            <el-tag style="margin: 5px" v-for="topic in scope.row.topics" :key="topic">{{ topic }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="图片">
          <template #default="scope">
            <el-row :gutter="10">
              <el-col :span="8" style="margin-bottom: 10px" v-for="(img,index) in scope.row.imgs" :key="img">
                <el-image preview-teleported :preview-src-list="[img.url]" style="width: 100%; " :src="img.url"></el-image>
              </el-col>
            </el-row>
          </template>
        </el-table-column>
        <el-table-column label="用户">
          <template #default="scope"><span v-if="scope.row.userId">{{
              state.userOptions.find(v => v.id === scope.row.userId) ? state.userOptions.find(v => v.id === scope.row.userId).name : ''
            }}</span></template>
        </el-table-column>
        <el-table-column prop="time" label="时间"></el-table-column>


        <el-table-column label="操作" width="180">
          <template #default="scope">
<!--            <el-button type="primary" @click="handleEdit(scope.row)" v-if="auths.includes('dynamic.edit')">编辑</el-button>-->
            <el-popconfirm title="您确定删除吗？" @confirm="del(scope.row.id)" v-if="auths.includes('dynamic.delete')">
              <template #reference>
                <el-button type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div style="margin: 10px 0">
      <el-pagination
          @current-change="load"
          @size-change="load"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          background
          :page-sizes="[2, 5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
      />
    </div>

    <el-dialog v-model="dialogFormVisible" title="动态信息" width="60%">
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
        <el-form-item prop="userId" label="用户">
          <el-select clearable v-model="state.form.userId" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in state.userOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>


        <el-form-item prop="time" label="时间">
          <el-date-picker style="width: 100%" v-model="state.form.time" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>


      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="save">
          保存
        </el-button>
      </span>
      </template>
    </el-dialog>

    <el-dialog v-model="viewShow" title="内容" width="40%">
      <div id="editor-content-view" class="editor-content-view" v-html="content" style="padding: 0 20px"></div>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="viewShow = false">关闭</el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>