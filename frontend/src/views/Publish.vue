<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategories, createProduct, updateProduct, getProduct, uploadImage } from '@/api'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const formRef = ref()
const categories = ref([])
const images = ref([])
const uploading = ref(false)
const fileInput = ref()

const form = reactive({ title: '', categoryId: null, price: null, description: '' })

const rules = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

function pickFiles() {
  fileInput.value.click()
}

async function onFiles(e) {
  const files = Array.from(e.target.files || [])
  if (!files.length) return
  uploading.value = true
  try {
    for (const f of files) {
      const res = await uploadImage(f)
      images.value.push(res.url)
    }
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

function removeImage(i) {
  images.value.splice(i, 1)
}

async function loadCategories() {
  try {
    categories.value = await getCategories()
  } catch { /* ignore */ }
}

async function loadProduct() {
  if (!isEdit.value) return
  try {
    const p = await getProduct(route.params.id)
    form.title = p.title
    form.categoryId = p.categoryId
    form.price = p.price
    form.description = p.description
    images.value = [...(p.images || [])]
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
}

async function submit() {
  await formRef.value.validate()
  const payload = {
    title: form.title,
    categoryId: form.categoryId,
    price: form.price,
    description: form.description,
    images: images.value
  }
  try {
    if (isEdit.value) {
      await updateProduct(route.params.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createProduct(payload)
      ElMessage.success('发布成功')
    }
    router.push('/profile')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  }
}

onMounted(() => {
  loadCategories()
  loadProduct()
})
</script>

<template>
  <div class="page-container">
    <div class="card form-card">
      <h2>{{ isEdit ? '编辑商品' : '发布商品' }}</h2>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="64" show-word-limit placeholder="例如：高等数学教材" />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="成色、使用情况、购买时间等" />
        </el-form-item>

        <el-form-item label="图片">
          <div class="imgs">
            <div v-for="(img, i) in images" :key="i" class="img-item">
              <el-image :src="img" fit="cover" class="thumb" />
              <span class="del" @click="removeImage(i)">×</span>
            </div>
            <div v-if="images.length < 9" class="add" @click="pickFiles">
              <el-icon><Plus /></el-icon>
            </div>
          </div>
          <span v-if="uploading" class="text-muted">上传中…</span>
          <input ref="fileInput" type="file" accept="image/*" multiple hidden @change="onFiles" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submit">{{ isEdit ? '保存修改' : '发布' }}</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.form-card {
  max-width: 640px;
  margin: 0 auto;
}
.imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.img-item {
  position: relative;
  width: 100px;
  height: 100px;
}
.thumb {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}
.del {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #f56c6c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
  line-height: 1;
}
.add {
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #909399;
  cursor: pointer;
}
.add:hover {
  border-color: var(--sh-primary);
  color: var(--sh-primary);
}
</style>