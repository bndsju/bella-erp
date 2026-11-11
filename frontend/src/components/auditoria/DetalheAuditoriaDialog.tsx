import { Link } from "react-router-dom";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import {
  ROTULO_MODULO,
  compararValores,
  listarValores,
  rotaDoRegistro,
  rotuloOperacao,
  varianteOperacao,
} from "@/lib/auditoriaRotulos";
import { formatarDataHora } from "@/lib/relatorioRotulos";
import type { RegistroAuditoria } from "@/types/auditoria";

interface DetalheAuditoriaDialogProps {
  registro: RegistroAuditoria | null;
  onOpenChange: (aberto: boolean) => void;
}

export function DetalheAuditoriaDialog({ registro, onOpenChange }: DetalheAuditoriaDialogProps) {
  const rota = registro ? rotaDoRegistro(registro.modulo, registro.entidadeId) : null;
  const temAntes = Boolean(registro?.valoresAnteriores && Object.keys(registro.valoresAnteriores).length > 0);
  const comparacao = registro && temAntes ? compararValores(registro) : [];
  const valores = registro && !temAntes ? listarValores(registro.novosValores) : [];

  return (
    <Dialog open={registro !== null} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[88vh] max-w-3xl overflow-y-auto">
        {registro && (
          <>
            <DialogHeader>
              <DialogTitle>
                {rotuloOperacao(registro.operacao, registro.modulo)} · {ROTULO_MODULO[registro.modulo]}
              </DialogTitle>
              <DialogDescription>Registro imutável: não pode ser editado nem apagado.</DialogDescription>
            </DialogHeader>

            <div>
              <div className="linha">
                <span>Data e hora</span>
                <span>{formatarDataHora(registro.dataHora)}</span>
              </div>
              <div className="linha">
                <span>Responsável</span>
                <span>{registro.usuario}</span>
              </div>
              <div className="linha">
                <span>Operação</span>
                <Badge variant={varianteOperacao(registro.operacao)}>
                  {rotuloOperacao(registro.operacao, registro.modulo)}
                </Badge>
              </div>
              <div className="linha">
                <span>Registro afetado</span>
                <span>
                  {ROTULO_MODULO[registro.modulo]} #{registro.entidadeId.slice(0, 8)}
                </span>
              </div>
              {registro.motivo && (
                <div className="linha">
                  <span>Motivo</span>
                  <span>{registro.motivo}</span>
                </div>
              )}
            </div>

            {temAntes ? (
              comparacao.length === 0 ? (
                <p>Nenhum campo visível mudou nesta operação.</p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Campo</TableHead>
                      <TableHead>Antes</TableHead>
                      <TableHead>Depois</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {comparacao.map((linha) => (
                      <TableRow key={linha.campo}>
                        <TableCell className="tabela__forte">{linha.campo}</TableCell>
                        <TableCell>{linha.antes}</TableCell>
                        <TableCell>{linha.depois}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )
            ) : valores.length === 0 ? (
              <p>Esta operação não guardou dados adicionais.</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Dados registrados</TableHead>
                    <TableHead>Valor</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {valores.map((linha) => (
                    <TableRow key={linha.campo}>
                      <TableCell className="tabela__forte">{linha.campo}</TableCell>
                      <TableCell>{linha.valor}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}

            <DialogFooter>
              {rota && (
                <Button variant="secondary" asChild>
                  <Link to={rota} onClick={() => onOpenChange(false)}>
                    Abrir registro
                  </Link>
                </Button>
              )}
              <Button onClick={() => onOpenChange(false)}>Fechar</Button>
            </DialogFooter>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
