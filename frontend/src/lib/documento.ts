const PESOS_CPF_1 = [10, 9, 8, 7, 6, 5, 4, 3, 2];
const PESOS_CPF_2 = [11, 10, 9, 8, 7, 6, 5, 4, 3, 2];
const PESOS_CNPJ_1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
const PESOS_CNPJ_2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];

export function apenasDigitos(valor: string): string {
  return valor.replace(/\D/g, "");
}

function todosDigitosIguais(digitos: string): boolean {
  return digitos.split("").every((c) => c === digitos[0]);
}

function calcularDigitoVerificador(base: string, pesos: number[]): number {
  const soma = pesos.reduce((total, peso, indice) => total + Number(base[indice]) * peso, 0);
  const resto = soma % 11;
  return resto < 2 ? 0 : 11 - resto;
}

export function validarCpf(valor: string): boolean {
  const digitos = apenasDigitos(valor);
  if (digitos.length !== 11 || todosDigitosIguais(digitos)) return false;

  const d1 = calcularDigitoVerificador(digitos, PESOS_CPF_1);
  const d2 = calcularDigitoVerificador(digitos.slice(0, 9) + d1, PESOS_CPF_2);

  return digitos.endsWith(`${d1}${d2}`);
}

export function validarCnpj(valor: string): boolean {
  const digitos = apenasDigitos(valor);
  if (digitos.length !== 14 || todosDigitosIguais(digitos)) return false;

  const d1 = calcularDigitoVerificador(digitos, PESOS_CNPJ_1);
  const d2 = calcularDigitoVerificador(digitos.slice(0, 12) + d1, PESOS_CNPJ_2);

  return digitos.endsWith(`${d1}${d2}`);
}

export function formatarCpf(valor: string): string {
  const digitos = apenasDigitos(valor).slice(0, 11);
  return digitos
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

export function formatarCnpj(valor: string): string {
  const digitos = apenasDigitos(valor).slice(0, 14);
  return digitos
    .replace(/(\d{2})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1/$2")
    .replace(/(\d{4})(\d{1,2})$/, "$1-$2");
}
