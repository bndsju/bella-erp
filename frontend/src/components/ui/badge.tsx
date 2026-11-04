import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { Slot } from "radix-ui"

import { cn } from "@/lib/utils"

/** Etiquetas do arquivo-mestre: pílula com contorno na cor do texto (ok, atenção, erro) ou neutra. */
const badgeVariants = cva("etiqueta", {
  variants: {
    variant: {
      default: "etiqueta--ok",
      ok: "etiqueta--ok",
      atencao: "etiqueta--atencao",
      destructive: "etiqueta--erro",
      erro: "etiqueta--erro",
      secondary: "",
      outline: "",
      ghost: "",
      link: "",
    },
  },
  defaultVariants: {
    variant: "default",
  },
})

function Badge({
  className,
  variant = "default",
  asChild = false,
  ...props
}: React.ComponentProps<"span"> &
  VariantProps<typeof badgeVariants> & { asChild?: boolean }) {
  const Comp = asChild ? Slot.Root : "span"

  return (
    <Comp
      data-slot="badge"
      data-variant={variant}
      className={cn(badgeVariants({ variant }), className)}
      {...props}
    />
  )
}

export { Badge, badgeVariants }
