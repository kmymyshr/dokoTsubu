/**
 * Reactアプリのエントリーポイント。
 *
 * Reactアプリのエントリーポイント。
 *
 * Phase22でJSPホストを廃止し、Spring MVC Controllerが返す最小HTMLの #root へReactをマウントする。
 * bodyのdata属性からcontextPathや画面種別を受け取り、APIクライアントと画面切り替えへ渡す。
 */
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";
import { configureApi, configureCsrfToken } from "./api.js";
import "./styles.css";

const rootElement = document.getElementById("root");
configureApi(document.body.dataset.contextPath || "");
configureCsrfToken(document.body.dataset.csrfToken || "");

createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>
);
