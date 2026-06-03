package ru.renthome.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StaticController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] bytes = css().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/css; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String css() {
        return """
                * { box-sizing: border-box; }
                body { margin: 0; font-family: Arial, sans-serif; color: #17211d; background: #f6f4ef; }
                a { color: #136f63; text-decoration: none; }
                main { max-width: 1180px; margin: 0 auto; padding: 30px 18px 60px; }
                .topbar { position: sticky; top: 0; z-index: 2; display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 14px 28px; background: rgba(255,255,255,.94); border-bottom: 1px solid #dedbd2; }
                .brand { font-weight: 800; font-size: 24px; color: #0e3b35; }
                nav { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
                button, input, select, textarea { font: inherit; }
                button { cursor: pointer; border: 0; border-radius: 6px; padding: 9px 13px; background: #163f3a; color: white; }
                .button { display: inline-flex; align-items: center; justify-content: center; border: 0; border-radius: 6px; padding: 11px 16px; background: #136f63; color: white; font-weight: 700; }
                .button.ghost { background: #e7ebe5; color: #183b35; }
                .button.small { padding: 8px 11px; }
                .button.full { width: 100%; }
                .inline { display: inline-flex; gap: 8px; align-items: center; margin: 0; }
                .hero { min-height: 430px; display: grid; grid-template-columns: minmax(0, 1.5fr) minmax(260px, .7fr); align-items: center; gap: 28px; padding: 48px 0; }
                .hero h1 { margin: 0 0 14px; font-size: 48px; line-height: 1.05; letter-spacing: 0; }
                .hero p { max-width: 640px; font-size: 18px; line-height: 1.55; }
                .hero-panel, .card, .form, .notice, .side-form, .review, .table-wrap { border: 1px solid #dedbd2; border-radius: 8px; background: #fff; }
                .hero-panel { display: grid; gap: 10px; padding: 22px; box-shadow: 0 18px 45px rgba(38,54,48,.12); }
                .eyebrow { text-transform: uppercase; color: #8a5a2b; font-size: 12px; font-weight: 800; letter-spacing: .08em; }
                .actions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 24px; }
                .section-head { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
                .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 18px; }
                .card { overflow: hidden; }
                .card img { width: 100%; aspect-ratio: 16/10; object-fit: cover; display: block; }
                .card-body { padding: 16px; }
                .card-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
                .card h3 { margin: 0; font-size: 20px; }
                .muted { color: #6d756e; }
                .icon-button { width: 38px; height: 38px; padding: 0; border-radius: 50%; background: #f1eadc; color: #8a5a2b; font-size: 22px; }
                .icon-button.active { background: #dff4e9; color: #087a4a; }
                .filters { display: grid; grid-template-columns: 1.2fr 1fr .8fr 1fr auto; gap: 10px; margin: 18px 0 24px; }
                input, select, textarea { width: 100%; border: 1px solid #cfcac0; border-radius: 6px; padding: 11px 12px; background: #fff; color: #17211d; }
                .form { display: grid; gap: 14px; padding: 20px; }
                label { display: grid; gap: 7px; font-weight: 700; }
                .narrow { max-width: 720px; margin: 0 auto; }
                .details { display: grid; grid-template-columns: 1.1fr .9fr 320px; gap: 24px; align-items: start; }
                .details-image { width: 100%; aspect-ratio: 4/3; object-fit: cover; border-radius: 8px; }
                .price { font-size: 28px; font-weight: 800; color: #136f63; }
                .review { padding: 14px 16px; margin: 10px 0; }
                .notice { padding: 16px; margin-bottom: 14px; }
                .danger { border-color: #e4a3a3; background: #fff1f1; color: #8b1d1d; }
                .success { border-color: #9ccdbb; background: #effaf4; color: #16443b; }
                .profile-card { display: flex; align-items: center; gap: 18px; padding: 20px; border: 1px solid #dedbd2; border-radius: 8px; background: #fff; margin-bottom: 16px; }
                .avatar { width: 72px; height: 72px; border-radius: 50%; display: grid; place-items: center; background: #136f63; color: #fff; font-size: 30px; font-weight: 800; }
                .profile-card h2 { margin: 2px 0 6px; }
                .profile-data { display: grid; gap: 10px; padding: 20px; border: 1px solid #dedbd2; border-radius: 8px; background: #fff; margin: 0 0 18px; }
                .profile-data div { display: grid; grid-template-columns: 140px 1fr; gap: 14px; }
                .profile-data dt { color: #6d756e; font-weight: 700; }
                .profile-data dd { margin: 0; overflow-wrap: anywhere; }
                .demo-logins { display: grid; grid-template-columns: repeat(auto-fit, minmax(170px, 1fr)); gap: 10px; margin-top: 14px; }
                .demo-logins form, .demo-logins button { width: 100%; }
                .table-wrap { overflow-x: auto; margin-bottom: 28px; }
                table { width: 100%; border-collapse: collapse; background: white; }
                th, td { padding: 12px; border-bottom: 1px solid #e8e4dc; text-align: left; vertical-align: top; }
                th { color: #58615b; font-size: 13px; }
                @media (max-width: 900px) {
                    .hero, .details { grid-template-columns: 1fr; }
                    .hero h1 { font-size: 36px; }
                    .filters { grid-template-columns: 1fr; }
                    .topbar { align-items: flex-start; flex-direction: column; }
                }
                """;
    }
}
