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
// state.userOptions = []
// request.get('/user').then(res => state.userOptions = res.data)


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
  request.post('/activity/del/batch', idArr).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

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

const dialogFormVisible = ref(false)

const rules = reactive({
  name: [
    {required: true, message: '请输入活动名称', trigger: 'blur'},
  ],
  fee: [
    {required: true, message: '请输入费用', trigger: 'blur'},
  ],
  location: [
    {required: true, message: '请输入地点', trigger: 'blur'},
  ],
  nums: [
    {required: true, message: '请输入可预约的数量', trigger: 'blur'},
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
  })
}

// 保存
const save = () => {
  ruleFormRef.value.validate(valid => {   // valid就是校验的结果
    if (valid) {
      state.form.content = valueHtml.value  // 富文本保存内容
      request.request({
        url: '/activity',
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


// 编辑
const handleEdit = (raw) => {
  dialogFormVisible.value = true
  nextTick(() => {
    ruleFormRef.value.resetFields()
    state.form = JSON.parse(JSON.stringify(raw))
    valueHtml.value = raw.content  // 富文本
  })
}


// 删除
const del = (id) => {
  request.delete('/activity/' + id).then(res => {
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
  window.open(`http://${config.serverUrl}/activity/export`)
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
const handleImgUploadSuccess = (res) => {
  state.form.img = res.data
  ElMessage.success('上传成功')
}
</script>

<template>
  <div>
    <div>
      <el-input v-model="name" placeholder="请输入活动名称" class="w300"/>
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
      <el-button type="success" @click="handleAdd" v-if="auths.includes('activity.add')">
        <el-icon style="vertical-align: middle">
          <Plus/>
        </el-icon>
        <span style="vertical-align: middle"> 新增 </span>
      </el-button>
      <el-upload
          v-if="auths.includes('activity.import')"
          class="ml5"
          :show-file-list="false"
          style="display: inline-block; position: relative; top: 3px"
          :action='`http://${config.serverUrl}/activity/import`'
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
      <el-button type="primary" @click="exportData" class="ml5" v-if="auths.includes('activity.export')">
        <el-icon style="vertical-align: middle">
          <Top/>
        </el-icon>
        <span style="vertical-align: middle"> 导出 </span>
      </el-button>
      <el-popconfirm title="您确定删除吗？" @confirm="confirmDelBatch" v-if="auths.includes('activity.deleteBatch')">
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
        <el-table-column type="index" label="编号" width="60px"></el-table-column>
        <el-table-column prop="name" label="活动名称"></el-table-column>
        <el-table-column label="活动内容">
          <template #default="scope">
            <el-button @click="view(scope.row.content)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150px" ></el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="150px" ></el-table-column>
        <el-table-column prop="nums" label="总人数"></el-table-column>
        <!--  剩余可预约人数 只在编辑时候出现，新增时不出现          -->
        <el-table-column prop="leftNums" label="剩余可预约人数"></el-table-column>
        <el-table-column prop="creator" label="发起人"></el-table-column>
        <el-table-column prop="location" label="活动地点"></el-table-column>
        <el-table-column prop="fee" label="活动经费"></el-table-column>
        <el-table-column label="活动图片" width="120px">
          <template #default="scope">
            <el-image preview-teleported style="width: 80px; height: 80px" :src="scope.row.img" v-if="scope.row.img" :preview-src-list="[scope.row.img]"></el-image>
          </template>
        </el-table-column>
        <el-table-column label="活动文件" width="140px" >
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


<!--        <el-table-column label="预约" width="90" fixed="right" v-if="auths.includes('activity.reserve')">-->
<!--          <template #default="scope">-->
<!--            <el-button-->
<!--                type="primary" @click="handleReserve(scope.row)" v-if="auths.includes('activity.reserve')">预约-->
<!--            </el-button>-->
<!--          </template>-->
<!--        </el-table-column>-->

        <el-table-column label="操作" width="200px" fixed="right" v-if="auths.includes('activity.edit') && auths.includes('activity.delete')" >
          <template #default="scope">
            <el-button type="primary" @click="handleEdit(scope.row)" v-if="auths.includes('activity.edit')">
              <el-icon style="vertical-align: middle">
                <edit/>
              </el-icon>
              <span style="vertical-align: middle; margin-left: 5px"> 编辑 </span>
            </el-button>
            <el-popconfirm title="您确定删除吗？" @confirm="del(scope.row.id)" v-if="auths.includes('activity.delete')">
              <template #reference>
                <el-button type="danger">
                  <el-icon style="vertical-align: middle">
                    <delete/>
                  </el-icon>
                  <span style="vertical-align: middle; margin-left: 5px"> 删除 </span>
                </el-button>
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

    <el-dialog v-model="dialogFormVisible" title="活动信息" width="40%">
      <el-form ref="ruleFormRef" :rules="rules" :model="state.form" label-width="120px" style="padding: 0 20px"
               status-icon>
        <el-form-item prop="name" label="名称">
          <el-input v-model="state.form.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="content" label="活动内容">
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
        <el-form-item prop="startTime" label="开始时间">
          <el-date-picker style="width: 100%" v-model="state.form.startTime" type="datetime" format="YYYY-MM-DD HH:mm"
                          value-format="YYYY-MM-DD HH:mm" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>
        <el-form-item prop="endTime" label="结束时间">
          <el-date-picker style="width: 100%" v-model="state.form.endTime" type="datetime" format="YYYY-MM-DD HH:mm"
                          value-format="YYYY-MM-DD HH:mm" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>
        <el-form-item prop="nums" label="总人数">
          <el-input v-model="state.form.nums" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="nums" label="剩余可预约人数" v-if="state.form.id">
          <el-input v-model="state.form.leftNums" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="location" label="活动地点">
          <el-input v-model="state.form.location" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="fee" label="活动经费">
          <el-input v-model="state.form.fee" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="img" label="活动图片">
          <el-upload :show-file-list="false" :action="`http://${config.serverUrl}/file/upload`" ref="file"
                     :headers="{ Authorization: token}" :on-success="handleImgUploadSuccess">
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="file" label="活动文件">
          <el-upload :show-file-list="false" :action="`http://${config.serverUrl}/file/upload`" ref="file"
                     :headers="{ Authorization: token}" :on-success="handleFileUploadSuccess">
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item prop="status" label="活动状态" v-if="state.form.id">
          <el-select clearable v-model="state.form.status" placeholder="请选择" style="width: 100%">
            <el-option
                v-for="item in state.statusOption"
                :key="item.value"
                :label="item.label"
                :value="item.value"></el-option>
          </el-select>
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