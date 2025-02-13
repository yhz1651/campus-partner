<script setup>
import {nextTick, reactive, ref} from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import config from "../../config";
import {useUserStore} from "@/stores/user";


const name = ref('')
const username = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const userStore = useUserStore()
const token = userStore.getBearerToken
const auths = userStore.getAuths
const currentUser = userStore.getUser

const state = reactive({
  tableData: [],
  form: {}
})

const valueHtml = ref('')  // 富文本内容

state.activityOptions = []
request.get('/activity').then(res => state.activityOptions = res.data)


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
  request.post('/reserve/del/batch', idArr).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const load = () => {
  request.get('/reserve/page', {
    params: {
      name: name.value,
      username: username.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.tableData = res.data.records
    total.value = res.data.total
  })

  request.get('/reserve/autoReview?autoReview=' + currentUser.autoReview).then(res => {})
}
load()  // 调用 load方法拿到后台数据

const reset = () => {
  name.value = ''
  username.value = ''
  load()
}

const dialogFormVisible = ref(false)

const rules = reactive({
  name: [
    {required: true, message: '请输入名称', trigger: 'blur'},
  ]
})
const ruleFormRef = ref()

const changeStatus = (row, status) => {
  const formData = {...row}
  formData.status = status
  request.request({
    url: '/reserve',
    method: 'put',
    data: formData
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('审核成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

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
        url: '/reserve',
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
  })
}

// 删除
const del = (id) => {
  request.delete('/reserve/' + id).then(res => {
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
  window.open(`http://${config.serverUrl}/reserve/export`)
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

const autoReview = () => {
  request.get('/reserve/autoReview?autoReview=' + currentUser.autoReview).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      console.log(currentUser.autoReview)
    } else {
      ElMessage.error(res.msg)
    }
  })
}


</script>

<template>
  <div>
    <div>
      <el-input v-model="name" placeholder="请输入活动名称" style="width: 200px;"/>
      <el-input v-model="username" placeholder="请输入用户名称" style="width: 200px; margin-left: 5px"/>
      <el-button type="primary" style="margin-left: 10px" @click="load">
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

    <div style="margin: 10px 0; display: flex">
      <div>
        <el-upload
            v-if="auths.includes('reserve.import')"
            class="ml5"
            :show-file-list="false"
            style="display: inline-block; position: relative; top: 3px"
            :action='`http://${config.serverUrl}/reserve/import`'
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
        <el-button type="primary" @click="exportData" class="ml5" v-if="auths.includes('reserve.export')">
          <el-icon style="vertical-align: middle">
            <Top/>
          </el-icon>
          <span style="vertical-align: middle"> 导出 </span>
        </el-button>
        <el-popconfirm title="您确定删除吗？" @confirm="confirmDelBatch" v-if="auths.includes('reserve.deleteBatch')">
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

      <div style="flex: 1; text-align: right" >
        <el-switch style="margin-left: 10px; margin-right: 20px"
                   v-model="currentUser.autoReview"
                   inline-prompt
                   active-text="自动审核"
                   inactive-text="手动审核"
                   size="large"
                   @change="autoReview"
                   v-if="auths.includes('reserve.approve')">
        </el-switch>
      </div>

    </div>

    <div style="margin: 10px 0">
      <el-table :data="state.tableData" stripe border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column type="index" label="编号" width="60"></el-table-column>
        <el-table-column prop="activityId" label="活动id"></el-table-column>
        <el-table-column label="活动名称">
          <template #default="scope">
            {{ scope.row.activity.name }}
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户id"></el-table-column>
        <el-table-column label="用户名称">
          <template #default="scope">
            {{ scope.row.user.name }}
          </template>
        </el-table-column>
        <el-table-column prop="time" label="预约时间"></el-table-column>
        <el-table-column prop="status" label="审核状态">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '待审核'">待审核</el-tag>
            <el-tag v-if="scope.row.status === '审核通过'" type="success">审核通过</el-tag>
            <el-tag v-if="scope.row.status === '审核不通过'" type="danger">审核不通过</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="审核" width="250"
                         v-if="auths.includes('reserve.approve') || auths.includes('reserve.disapprove')">
          <template #default="scope">
            <el-button type="success" @click="changeStatus(scope.row, '审核通过')"
                       v-if="auths.includes('reserve.approve')" :disabled="scope.row.status !== '待审核'">审核通过
            </el-button>
            <el-button type="warning" @click="changeStatus(scope.row, '审核不通过')"
                       v-if="auths.includes('reserve.disapprove')" :disabled="scope.row.status !== '待审核'">审核不通过
            </el-button>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-popconfirm title="您确定删除吗？" @confirm="del(scope.row.id)" v-if="auths.includes('reserve.delete')">
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

  </div>
</template>