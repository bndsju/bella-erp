import { z } from "zod";

const camposComunsProduto = {
  codigoBarras: z.string().optional(),
  nome: z.string().min(1, "Nome é obrigatório"),
  descricao: z.string().optional(),
  categoriaId: z.string().min(1, "Categoria é obrigatória"),
  unidadeMedidaId: z.string().min(1, "Unidade de medida é obrigatória"),
  precoCusto: z.number({ message: "Preço de custo é obrigatório" }).min(0, "Preço de custo não pode ser negativo"),
  precoVenda: z.number({ message: "Preço de venda é obrigatório" }).min(0, "Preço de venda não pode ser negativo"),
  estoqueMinimo: z.number({ message: "Estoque mínimo é obrigatório" }).min(0, "Estoque mínimo não pode ser negativo"),
};

export const produtoCadastroSchema = z.object({
  codigoInterno: z.string().min(1, "Código interno é obrigatório"),
  ...camposComunsProduto,
});

export const produtoEdicaoSchema = z.object(camposComunsProduto);

export type ProdutoCadastroFormValues = z.infer<typeof produtoCadastroSchema>;
export type ProdutoEdicaoFormValues = z.infer<typeof produtoEdicaoSchema>;

export const categoriaSchema = z.object({
  nome: z.string().min(1, "Nome é obrigatório"),
});

export type CategoriaFormValues = z.infer<typeof categoriaSchema>;

export const unidadeMedidaSchema = z.object({
  nome: z.string().min(1, "Nome é obrigatório"),
  sigla: z.string().min(1, "Sigla é obrigatória"),
});

export type UnidadeMedidaFormValues = z.infer<typeof unidadeMedidaSchema>;
