import { Link } from "react-router-dom";
import { Card } from "@/components/ui/card";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

export function SemAcesso() {
  return (
    <>
      <CabecalhoPagina titulo="Sem acesso" />
      <Card>
        <div className="vazio">
          <h3>Seu perfil não tem acesso a esta área</h3>
          <p>
            Se você precisa dela, peça a um administrador para ajustar o seu perfil. <Link to="/">Voltar ao início</Link>
          </p>
        </div>
      </Card>
    </>
  );
}
