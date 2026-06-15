import { ref } from 'vue'
import type { UploadRawFile } from 'element-plus'

export function useUpload() {
  const isUploading = ref(false)
  const progress = ref(0)
  const uploadedUrls = ref<string[]>([])

  async function uploadFile(file: UploadRawFile, endpoint: string = '/media/upload'): Promise<string | null> {
    isUploading.value = true
    progress.value = 0

    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('accessToken')}`,
        },
        body: formData,
      })

      if (!response.ok) {
        throw new Error('Upload failed')
      }

      const data = await response.json()
      uploadedUrls.value.push(data.url)
      return data.url
    } catch (error) {
      console.error('Upload error:', error)
      return null
    } finally {
      isUploading.value = false
      progress.value = 0
    }
  }

  async function uploadMultiple(files: File[], endpoint: string = '/media/upload'): Promise<string[]> {
    const urls: string[] = []
    
    for (const file of files) {
      const url = await uploadFile(file as UploadRawFile, endpoint)
      if (url) {
        urls.push(url)
      }
    }
    
    return urls
  }

  function clearUploaded() {
    uploadedUrls.value = []
  }

  return {
    isUploading,
    progress,
    uploadedUrls,
    uploadFile,
    uploadMultiple,
    clearUploaded,
  }
}
