import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Link } from "react-router-dom";
import { Card, CardTitle } from "@/components/ui/card";
import { EnderecoFields } from "./EnderecoFields";
import { formatarCnpj, formatarCpf } from "@/lib/documento";
import { formatarTelefone } from "@/lib/masks";
import {
  clientePessoaFisicaSchema,
  clientePessoaJuridicaSchema,
  type ClienteFormValoresIniciais,
  type ClientePessoaFisicaFormValues,
  type ClientePessoaJuridicaFormValues,
} from "@/lib/clienteSchema";
import type { ClienteRequest } from "@/types/cliente";

const enderecoPadrao = {
  cep: "",
  logradouro: "",
  numero: "",
  complemento: "",
  bairro: "",
  cidade: "",
  uf: "",
};

interface ClienteFormFieldsProps {
  tipoPessoa: "PF" | "PJ";
  valoresIniciais?: ClienteFormValoresIniciais;
  documentoBloqueado?: boolean;
  enviando: boolean;
  onSubmit: (dados: ClienteRequest) => void;
}

export function ClienteFormFields(props: ClienteFormFieldsProps) {
  return props.tipoPessoa === "PF" ? (
    <FormularioPessoaFisica {...props} />
  ) : (
    <FormularioPessoaJuridica {...props} />
  );
}

function CamposComunsInline({
  celular,
  telefone,
  email,
  observacoes,
}: {
  celular: React.ReactNode;
  telefone: React.ReactNode;
  email: React.ReactNode;
  observacoes: React.ReactNode;
}) {
  return (
    <>
      <Card>
        <CardTitle>Contato</CardTitle>
        <div className="grade grade--2">
          {celular}
          {telefone}
          {email}
        </div>
      </Card>
      <Card>
        <CardTitle>Endereço</CardTitle>
        <EnderecoFields />
      </Card>
      <Card>{observacoes}</Card>
    </>
  );
}

function AcoesDoFormulario({ enviando }: { enviando: boolean }) {
  return (
    <div className="acoes">
      <Button variant="secondary" asChild>
        <Link to="/clientes">Cancelar</Link>
      </Button>
      <Button type="submit" disabled={enviando}>
        {enviando ? "Salvando..." : "Salvar cliente"}
      </Button>
    </div>
  );
}

function FormularioPessoaFisica({
  valoresIniciais,
  documentoBloqueado,
  enviando,
  onSubmit,
}: ClienteFormFieldsProps) {
  const form = useForm<ClientePessoaFisicaFormValues>({
    resolver: zodResolver(clientePessoaFisicaSchema),
    defaultValues: {
      tipoPessoa: "PF",
      nomeCompleto: valoresIniciais?.nomeCompleto ?? "",
      dataNascimento: valoresIniciais?.dataNascimento ?? "",
      cpf: valoresIniciais?.cpf ?? "",
      celular: valoresIniciais?.celular ?? "",
      email: valoresIniciais?.email ?? "",
      telefone: valoresIniciais?.telefone ?? "",
      endereco: valoresIniciais?.endereco ?? enderecoPadrao,
      observacoes: valoresIniciais?.observacoes ?? "",
    },
  });

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit((dados) => onSubmit(dados as ClienteRequest))}
        className="contents"
      >
        <Card>
          <CardTitle>Dados do cliente</CardTitle>
          <div className="grade grade--2">
          <FormField
            control={form.control}
            name="nomeCompleto"
            render={({ field }) => (
              <FormItem className="col-span-full">
                <FormLabel>Nome completo</FormLabel>
                <FormControl>
                  <Input placeholder="Maria da Silva" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="dataNascimento"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Data de nascimento</FormLabel>
                <FormControl>
                  <Input type="date" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="cpf"
            render={({ field }) => (
              <FormItem>
                <FormLabel>CPF</FormLabel>
                <FormControl>
                  <Input
                    placeholder="000.000.000-00"
                    disabled={documentoBloqueado}
                    {...field}
                    onChange={(e) => field.onChange(formatarCpf(e.target.value))}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          </div>
        </Card>

        <CamposComunsInline
          celular={
            <FormField
              control={form.control}
              name="celular"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Celular</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="(11) 99999-8888"
                      {...field}
                      onChange={(e) => field.onChange(formatarTelefone(e.target.value))}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          }
          telefone={
            <FormField
              control={form.control}
              name="telefone"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Telefone</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="(11) 3333-4444"
                      {...field}
                      onChange={(e) => field.onChange(formatarTelefone(e.target.value))}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          }
          email={
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem className="col-span-full">
                  <FormLabel>Email</FormLabel>
                  <FormControl>
                    <Input type="email" placeholder="contato@exemplo.com" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          }
          observacoes={
            <FormField
              control={form.control}
              name="observacoes"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Observações</FormLabel>
                  <FormControl>
                    <Textarea rows={3} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          }
        />

        <AcoesDoFormulario enviando={enviando} />
      </form>
    </Form>
  );
}

function FormularioPessoaJuridica({
  valoresIniciais,
  documentoBloqueado,
  enviando,
  onSubmit,
}: ClienteFormFieldsProps) {
  const form = useForm<ClientePessoaJuridicaFormValues>({
    resolver: zodResolver(clientePessoaJuridicaSchema),
    defaultValues: {
      tipoPessoa: "PJ",
      razaoSocial: valoresIniciais?.razaoSocial ?? "",
      nomeFantasia: valoresIniciais?.nomeFantasia ?? "",
      cnpj: valoresIniciais?.cnpj ?? "",
      inscricaoEstadual: valoresIniciais?.inscricaoEstadual ?? "",
      inscricaoMunicipal: valoresIniciais?.inscricaoMunicipal ?? "",
      celular: valoresIniciais?.celular ?? "",
      email: valoresIniciais?.email ?? "",
      telefone: valoresIniciais?.telefone ?? "",
      endereco: valoresIniciais?.endereco ?? enderecoPadrao,
      observacoes: valoresIniciais?.observacoes ?? "",
    },
  });

  return (
    <Form {...form}>
      <form
        onSubmit={form.handleSubmit((dados) => onSubmit(dados as ClienteRequest))}
        className="contents"
      >
        <Card>
          <CardTitle>Dados do cliente</CardTitle>
          <div className="grade grade--2">
          <FormField
            control={form.control}
            name="razaoSocial"
            render={({ field }) => (
              <FormItem className="col-span-full">
                <FormLabel>Razão social</FormLabel>
                <FormControl>
                  <Input placeholder="Padaria Pão Quente Ltda" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="nomeFantasia"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Nome fantasia</FormLabel>
                <FormControl>
                  <Input placeholder="Pão Quente" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="cnpj"
            render={({ field }) => (
              <FormItem>
                <FormLabel>CNPJ</FormLabel>
                <FormControl>
                  <Input
                    placeholder="00.000.000/0000-00"
                    disabled={documentoBloqueado}
                    {...field}
                    onChange={(e) => field.onChange(formatarCnpj(e.target.value))}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="inscricaoEstadual"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Inscrição estadual</FormLabel>
                <FormControl>
                  <Input {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="inscricaoMunicipal"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Inscrição municipal</FormLabel>
                <FormControl>
                  <Input {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          </div>
        </Card>

        <CamposComunsInline
          celular={
            <FormField
              control={form.control}
              name="celular"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Celular</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="(11) 99999-8888"
                      {...field}
                      onChange={(e) => field.onChange(formatarTelefone(e.target.value))}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          }
          telefone={
            <FormField
              control={form.control}
              name="telefone"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Telefone</FormLabel>
                  <FormControl>
                    <Input
                      placeholder="(11) 3333-4444"
                      {...field}
                      onChange={(e) => field.onChange(formatarTelefone(e.target.value))}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          }
          email={
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem className="col-span-full">
                  <FormLabel>Email</FormLabel>
                  <FormControl>
                    <Input type="email" placeholder="contato@exemplo.com" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          }
          observacoes={
            <FormField
              control={form.control}
              name="observacoes"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Observações</FormLabel>
                  <FormControl>
                    <Textarea rows={3} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
          }
        />

        <AcoesDoFormulario enviando={enviando} />
      </form>
    </Form>
  );
}
