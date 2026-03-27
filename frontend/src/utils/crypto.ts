/**
 * 与后端约定：SHA-256(loginName + "easy_shm" + 明文密码)，hex 小写。
 * 新建用户、重置密码、登录等场景在传输前调用。
 */
export async function hashPassword(loginName: string, plainPassword: string): Promise<string> {
  const raw = loginName + 'easy_shm' + plainPassword
  const buf = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(raw))
  return Array.from(new Uint8Array(buf))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('')
}
