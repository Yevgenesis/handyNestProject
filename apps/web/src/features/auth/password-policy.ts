export const PASSWORD_PATTERN =
  /^(?=.*\p{N})(?=.*\p{Ll})(?=.*\p{Lu})(?=.*[^\p{L}\p{N}\s])\S{8,20}$/u;

export function isPasswordValid(password: string) {
  return PASSWORD_PATTERN.test(password);
}
