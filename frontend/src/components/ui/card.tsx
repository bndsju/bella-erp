import * as React from "react"

import { cn } from "@/lib/utils"

type VarianteCartao = "padrao" | "destaque" | "suave"

const CLASSE_VARIANTE: Record<VarianteCartao, string> = {
  padrao: "",
  destaque: "cartao--destaque",
  suave: "cartao--suave",
}

/** Cartão do arquivo-mestre: branco (preto no Dark), 20 px de raio, 22 px de padding, sem sombra. */
function Card({
  className,
  variante = "padrao",
  ...props
}: React.ComponentProps<"div"> & { variante?: VarianteCartao }) {
  return <div data-slot="card" className={cn("cartao", CLASSE_VARIANTE[variante], className)} {...props} />
}

function CardHeader({ className, ...props }: React.ComponentProps<"div">) {
  return <div data-slot="card-header" className={cn("flex items-start justify-between gap-3", className)} {...props} />
}

/** Título de cartão: <h2> para herdar ".cartao h2" (17 px, 800, Verde Mata; Sálvia no Dark). */
function CardTitle({ className, ...props }: React.ComponentProps<"h2">) {
  return <h2 data-slot="card-title" className={className} {...props} />
}

function CardDescription({ className, ...props }: React.ComponentProps<"p">) {
  return <p data-slot="card-description" className={cn("text-auxiliar", className)} {...props} />
}

function CardAction({ className, ...props }: React.ComponentProps<"div">) {
  return <div data-slot="card-action" className={cn("shrink-0", className)} {...props} />
}

function CardContent({ className, ...props }: React.ComponentProps<"div">) {
  return <div data-slot="card-content" className={className} {...props} />
}

function CardFooter({ className, ...props }: React.ComponentProps<"div">) {
  return <div data-slot="card-footer" className={cn("mt-4 flex items-center gap-2", className)} {...props} />
}

export {
  Card,
  CardHeader,
  CardFooter,
  CardTitle,
  CardAction,
  CardDescription,
  CardContent,
}
