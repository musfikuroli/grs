package com.grs.api.config.security;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grs.api.model.OISFUserType;
import com.grs.api.model.OfficeInformation;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.core.service.FcmService;
import com.grs.utils.BeanUtil;
import com.grs.utils.Constant;
import com.grs.utils.CookieUtil;
import com.grs.utils.StringUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Tanvir on 4/18/2017.
 */
@Slf4j
public class TokenAuthenticationServiceUtil {

    private final static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void addAuthentication(Authentication authentication,
                                         HttpServletRequest request,
                                         HttpServletResponse response) throws IOException, ServletException {
        UserInformation userInformation;
        String name;
        Set<String> permissionNamesSet;
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            name = authentication.getName();
            permissionNamesSet = authentication.getAuthorities()
                    .stream()
                    .map(permission -> permission.getAuthority())
                    .collect(Collectors.toSet());
            userInformation = userDetails.getUserInformation();

        } catch (Exception e) {
            CustomAuthenticationToken token = (CustomAuthenticationToken) authentication;
            name = token.getName();
            permissionNamesSet = token.getAuthorities()
                    .stream()
                    .map(permission -> permission.getAuthority())
                    .collect(Collectors.toSet());
            userInformation = token.getUserInformation();
        }

        String deviceToken = request.getParameter("device_token");
        if (StringUtil.isValidString(deviceToken)) {
            FcmService fcmService = BeanUtil.bean(FcmService.class);
            fcmService.registerDeviceToken(deviceToken, name);
            userInformation.setIsMobileLogin(true);
        } else {
            userInformation.setIsMobileLogin(false);
        }
        String JWT = constuctJwtToken(name, permissionNamesSet, userInformation);
//        String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDAwMTU0MTUiLCJwZXJtaXNzaW9ucyI6WyJBRERfU0VSVklDRVMiLCJFRElUX0NJVElaRU5fQ0hBUlRFUiIsIk9GRkxJTkVfR1JJRVZBTkNFX1VQTE9BRCIsIkVESVRfU0VSVklDRVMiLCJBRERfUFVCTElDX0dSSUVWQU5DRVMiLCJBRERfT0ZGSUNJQUxfR1JJRVZBTkNFUyIsIlZJRVdfU1VHR0VTVElPTiIsIlZJRVdfUFVCTElDX0dSSUVWQU5DRVMiLCJWSUVXX1JFR0lTVEVSIiwiVklFV19TVEFGRl9HUklFVkFOQ0VTIiwiVklFV19PRkZJQ0lBTF9HUklFVkFOQ0VTIiwiQUREX1NUQUZGX0dSSUVWQU5DRVMiLCJWSUVXX0NJVElaRU5fQ0hBUlRFUiIsIlZJRVdfU0VSVklDRVMiLCJBRERfQ0lUSVpFTl9DSEFSVEVSIl0sInVzZXJfaW5mbyI6eyJ1c2VySWQiOjIzNDUsInVzZXJuYW1lIjoiMTAwMDAwMDE1NDE1IiwidXNlclR5cGUiOiJPSVNGX1VTRVIiLCJvaXNmVXNlclR5cGUiOiJHUk8iLCJncnNVc2VyVHlwZSI6bnVsbCwib2ZmaWNlSW5mb3JtYXRpb24iOnsib2ZmaWNlSWQiOjkzLCJvZmZpY2VOYW1lQmFuZ2xhIjoi4Kac4KeH4Kay4Ka-IOCmquCnjeCmsOCmtuCmvuCmuOCmleCnh-CmsCDgppXgpr7gprDgp43gpq_gpr7gprLgp58sIOCmmuCmvuCmgeCmpuCmquCngeCmsCIsIm9mZmljZU5hbWVFbmdsaXNoIjoiT2ZmaWNlIG9mIHRoZSBEZXB1dHkgQ29tbWlzc2lvbmVyLCBDaGFuZHB1ciIsIm9mZmljZU1pbmlzdHJ5SWQiOjUsIm9mZmljZU9yaWdpbklkIjoxNiwibmFtZSI6IuCmruCni-CmueCmvuCmruCnjeCmruCmpiDgpobgpqzgpqbgp4HgprIg4Ka54Ka-4KaHIiwiZGVzaWduYXRpb24iOiLgpoXgpqTgpr_gprDgpr_gppXgp43gpqQg4Kac4KeH4Kay4Ka-IOCmquCnjeCmsOCmtuCmvuCmuOCmlSIsImVtcGxveWVlUmVjb3JkSWQiOjIzMjMsIm9mZmljZVVuaXRPcmdhbm9ncmFtSWQiOjg4NzEsImxheWVyTGV2ZWwiOjUsImdlb0RpdmlzaW9uSWQiOjIsImdlb0Rpc3RyaWN0SWQiOjksImdlb1VwYXppbGFJZCI6bnVsbH0sImlzQXBwZWFsT2ZmaWNlciI6dHJ1ZSwiaXNPZmZpY2VBZG1pbiI6ZmFsc2UsImlzQ2VudHJhbERhc2hib2FyZFVzZXIiOmZhbHNlLCJpc0NlbGxHUk8iOmZhbHNlLCJpc01vYmlsZUxvZ2luIjpmYWxzZX19.38LlQHwtyiPoQ7CLWuUnY79xF0zadxyu04pp574wEf1OD5WFKGcdnKq-AxAn2rxZTjkpzvtBE6xIR1nfLSy-iQ";
// abdul hai sir
//        String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDAwMTc3MTQiLCJwZXJtaXNzaW9ucyI6WyJWSUVXX1BVQkxJQ19HUklFVkFOQ0VTIiwiVklFV19TVEFGRl9HUklFVkFOQ0VTIiwiQUREX1BVQkxJQ19HUklFVkFOQ0VTIl0sInVzZXJfaW5mbyI6eyJ1c2VySWQiOjk5MDcsInVzZXJuYW1lIjoiMTAwMDAwMDE3NzE0IiwidXNlclR5cGUiOiJPSVNGX1VTRVIiLCJvaXNmVXNlclR5cGUiOiJTRVJWSUNFX09GRklDRVIiLCJncnNVc2VyVHlwZSI6bnVsbCwib2ZmaWNlSW5mb3JtYXRpb24iOnsib2ZmaWNlSWQiOjkzLCJvZmZpY2VOYW1lQmFuZ2xhIjoi4Kac4KeH4Kay4Ka-IOCmquCnjeCmsOCmtuCmvuCmuOCmleCnh-CmsCDgppXgpr7gprDgp43gpq_gpr7gprLgp58sIOCmmuCmvuCmgeCmpuCmquCngeCmsCIsIm9mZmljZU5hbWVFbmdsaXNoIjoiT2ZmaWNlIG9mIHRoZSBEZXB1dHkgQ29tbWlzc2lvbmVyLCBDaGFuZHB1ciIsIm9mZmljZU1pbmlzdHJ5SWQiOjUsIm9mZmljZU9yaWdpbklkIjpudWxsLCJuYW1lIjoi4Ka24KeH4KaWIOCmruCnh-CmnOCmrOCmvuCmuSAtIOCmieCmsi0g4Ka44Ka-4Kas4KeH4Kaw4Ka_4KaoIiwiZGVzaWduYXRpb24iOiLgprjgprngppXgpr7gprDgp4Ag4KaV4Kau4Ka_4Ka24Kao4Ka-4KawIiwiZW1wbG95ZWVSZWNvcmRJZCI6OTg3OSwib2ZmaWNlVW5pdE9yZ2Fub2dyYW1JZCI6ODg3NywibGF5ZXJMZXZlbCI6NSwiZ2VvRGl2aXNpb25JZCI6MiwiZ2VvRGlzdHJpY3RJZCI6OSwiZ2VvVXBhemlsYUlkIjowfSwiaXNBcHBlYWxPZmZpY2VyIjpmYWxzZSwiaXNPZmZpY2VBZG1pbiI6ZmFsc2UsImlzQ2VudHJhbERhc2hib2FyZFVzZXIiOmZhbHNlLCJpc0NlbGxHUk8iOmZhbHNlLCJpc01vYmlsZUxvZ2luIjpmYWxzZX19.wcVxUsacNZMhNAm4-akjxiRmDg1mTcInl9vEsvs5sI7YoJ4hR9Dj2DyRk8L1CaKt_UiJCwFNC7P8SJ-oYJNGpA";
// mokhles sir
//        String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDAwMTUzODIiLCJwZXJtaXNzaW9ucyI6WyJBRERfU0VSVklDRVMiLCJWSUVXX01JU1NJTkdfT0ZGSUNFUl9UWVBFIiwiRURJVF9DSVRJWkVOX0NIQVJURVIiLCJPRkZMSU5FX0dSSUVWQU5DRV9VUExPQUQiLCJFRElUX1NFUlZJQ0VTIiwiQUREX1BVQkxJQ19HUklFVkFOQ0VTIiwiQUREX09GRklDSUFMX0dSSUVWQU5DRVMiLCJWSUVXX1NVR0dFU1RJT04iLCJWSUVXX1BVQkxJQ19HUklFVkFOQ0VTIiwiVklFV19SRUdJU1RFUiIsIlZJRVdfU1RBRkZfR1JJRVZBTkNFUyIsIlZJRVdfT0ZGSUNJQUxfR1JJRVZBTkNFUyIsIkFERF9TVEFGRl9HUklFVkFOQ0VTIiwiVklFV19DSVRJWkVOX0NIQVJURVIiLCJWSUVXX0xBWUVSV0lTRV9DVVNUT01fUkVQT1JUIiwiVklFV19TRVJWSUNFUyIsIkFERF9DSVRJWkVOX0NIQVJURVIiXSwidXNlcl9pbmZvIjp7InVzZXJJZCI6NzQ0MCwidXNlcm5hbWUiOiIxMDAwMDAwMTUzODIiLCJ1c2VyVHlwZSI6Ik9JU0ZfVVNFUiIsIm9pc2ZVc2VyVHlwZSI6IkdSTyIsImdyc1VzZXJUeXBlIjpudWxsLCJvZmZpY2VJbmZvcm1hdGlvbiI6eyJvZmZpY2VJZCI6MjgsIm9mZmljZU5hbWVCYW5nbGEiOiLgpq7gpqjgp43gpqTgp43gprDgpr_gpqrgprDgpr_gprfgpqYg4Kas4Ka_4Kat4Ka-4KaXICIsIm9mZmljZU5hbWVFbmdsaXNoIjoiTWluaXN0cnkiLCJvZmZpY2VNaW5pc3RyeUlkIjo0LCJvZmZpY2VPcmlnaW5JZCI6NDIsIm5hbWUiOiLgpq7gp4vgpoMg4Kau4KaW4Kay4KeH4Kab4KeB4KawIOCmsOCmueCmruCmvuCmqCIsImRlc2lnbmF0aW9uIjoi4KaJ4Kaq4Ka44Kaa4Ka_4KasIiwiZW1wbG95ZWVSZWNvcmRJZCI6ODEzNzMsIm9mZmljZVVuaXRPcmdhbm9ncmFtSWQiOjEyNDU0NywibGF5ZXJMZXZlbCI6MSwiZ2VvRGl2aXNpb25JZCI6MywiZ2VvRGlzdHJpY3RJZCI6MTh9LCJpc0FwcGVhbE9mZmljZXIiOnRydWUsImlzT2ZmaWNlQWRtaW4iOmZhbHNlLCJpc0NlbnRyYWxEYXNoYm9hcmRVc2VyIjp0cnVlLCJpc0NlbGxHUk8iOnRydWUsImlzTW9iaWxlTG9naW4iOmZhbHNlLCJpc015R292TG9naW4iOm51bGwsInRva2VuIjpudWxsfX0.hdLo6U06Nm3fIO8k6qYRdaF-aSDw8t2zTwg_Xp4bW-8mU2y_JCLFrA4hAgrpC3QGQvwGImKpnmt3RFtM7QnuSA";
//       mokhles sir transferred token
//        String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDAwMTUzODIiLCJwZXJtaXNzaW9ucyI6WyJWSUVXX1BVQkxJQ19HUklFVkFOQ0VTIiwiVklFV19NSVNTSU5HX09GRklDRVJfVFlQRSIsIlZJRVdfVElNRVdJU0VfQ09NUExBSU5BTlRfUkVQT1JUIiwiVklFV19TVEFGRl9HUklFVkFOQ0VTIiwiQUREX1BVQkxJQ19HUklFVkFOQ0VTIiwiVklFV19MQVlFUldJU0VfQ1VTVE9NX1JFUE9SVCJdLCJ1c2VyX2luZm8iOnsidXNlcklkIjo3NDQwLCJ1c2VybmFtZSI6IjEwMDAwMDAxNTM4MiIsInVzZXJUeXBlIjoiT0lTRl9VU0VSIiwib2lzZlVzZXJUeXBlIjoiU0VSVklDRV9PRkZJQ0VSIiwiZ3JzVXNlclR5cGUiOm51bGwsIm9mZmljZUluZm9ybWF0aW9uIjp7Im9mZmljZUlkIjoyOCwib2ZmaWNlTmFtZUJhbmdsYSI6IuCmruCmqOCnjeCmpOCnjeCmsOCmv-CmquCmsOCmv-Cmt-CmpiDgpqzgpr_gpq3gpr7gppcgIiwib2ZmaWNlTmFtZUVuZ2xpc2giOiJNaW5pc3RyeSIsIm9mZmljZU1pbmlzdHJ5SWQiOjQsIm9mZmljZU9yaWdpbklkIjo0MiwibmFtZSI6IuCmruCni-CmgyDgpq7gppbgprLgp4fgppvgp4HgprAg4Kaw4Ka54Kau4Ka-4KaoIiwiZGVzaWduYXRpb24iOiLgpongpqrgprjgpprgpr_gpqwiLCJlbXBsb3llZVJlY29yZElkIjo4MTM3Mywib2ZmaWNlVW5pdE9yZ2Fub2dyYW1JZCI6MTEyODIsImxheWVyTGV2ZWwiOjEsImdlb0RpdmlzaW9uSWQiOjMsImdlb0Rpc3RyaWN0SWQiOjE4fSwiaXNBcHBlYWxPZmZpY2VyIjp0cnVlLCJpc09mZmljZUFkbWluIjpmYWxzZSwiaXNDZW50cmFsRGFzaGJvYXJkVXNlciI6ZmFsc2UsImlzQ2VsbEdSTyI6dHJ1ZSwiaXNNb2JpbGVMb2dpbiI6ZmFsc2UsImlzTXlHb3ZMb2dpbiI6bnVsbCwidG9rZW4iOm51bGx9fQ.q8h5dLEpLHbVvmVZp_mjQOzDdvfwPawgLxMNsuBhLDuBvo2y2LDnLRiLoMEimSlAgNOFEZYH3N-POlARE5ETxg";
// mannan sir medhamonon
//        String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDAwMDU4MTciLCJwZXJtaXNzaW9ucyI6WyJBRERfU0VSVklDRVMiLCJFRElUX0NJVElaRU5fQ0hBUlRFUiIsIk9GRkxJTkVfR1JJRVZBTkNFX1VQTE9BRCIsIkVESVRfU0VSVklDRVMiLCJBRERfUFVCTElDX0dSSUVWQU5DRVMiLCJBRERfT0ZGSUNJQUxfR1JJRVZBTkNFUyIsIlZJRVdfU1VHR0VTVElPTiIsIlZJRVdfUFVCTElDX0dSSUVWQU5DRVMiLCJWSUVXX1JFR0lTVEVSIiwiVklFV19TVEFGRl9HUklFVkFOQ0VTIiwiVklFV19PRkZJQ0lBTF9HUklFVkFOQ0VTIiwiQUREX1NUQUZGX0dSSUVWQU5DRVMiLCJWSUVXX0NJVElaRU5fQ0hBUlRFUiIsIlZJRVdfU0VSVklDRVMiLCJBRERfQ0lUSVpFTl9DSEFSVEVSIl0sInVzZXJfaW5mbyI6eyJ1c2VySWQiOjM0MzYwLCJ1c2VybmFtZSI6IjEwMDAwMDAwNTgxNyIsInVzZXJUeXBlIjoiT0lTRl9VU0VSIiwib2lzZlVzZXJUeXBlIjoiR1JPIiwiZ3JzVXNlclR5cGUiOm51bGwsIm9mZmljZUluZm9ybWF0aW9uIjp7Im9mZmljZUlkIjoyMTY0LCJvZmZpY2VOYW1lQmFuZ2xhIjoi4Kaq4KeN4Kaw4Kas4Ka-4Ka44KeAIOCmleCmsuCnjeCmr-CmvuCmoyDgppMg4Kas4KeI4Kam4KeH4Ka24Ka_4KaVIOCmleCmsOCnjeCmruCmuOCmguCmuOCnjeCmpeCmvuCmqCDgpq7gpqjgp43gpqTgp43gprDgpqPgpr7gprLgp58iLCJvZmZpY2VOYW1lRW5nbGlzaCI6Ik1pbmlzdHJ5IG9mIEV4cGF0cmlhdGUgV2VsZmFyZSAmIE92ZXJzZWFzIEVtcGxveW1lbnQiLCJvZmZpY2VNaW5pc3RyeUlkIjo0OSwib2ZmaWNlT3JpZ2luSWQiOjE4NSwibmFtZSI6IuCmruCni-CmgyDgpobgpqzgp43gpqbgp4HgprIg4Kau4Ka-4Kao4KeN4Kao4Ka-4KaoIiwiZGVzaWduYXRpb24iOiLgpoXgpqTgpr_gprDgpr_gppXgp43gpqQg4Ka44Kaa4Ka_4KasIiwiZW1wbG95ZWVSZWNvcmRJZCI6MTA4ODAyLCJvZmZpY2VVbml0T3JnYW5vZ3JhbUlkIjo3ODg3MywibGF5ZXJMZXZlbCI6MSwiZ2VvRGl2aXNpb25JZCI6MywiZ2VvRGlzdHJpY3RJZCI6MTh9LCJpc0FwcGVhbE9mZmljZXIiOnRydWUsImlzT2ZmaWNlQWRtaW4iOmZhbHNlLCJpc0NlbnRyYWxEYXNoYm9hcmRVc2VyIjpmYWxzZSwiaXNDZWxsR1JPIjpmYWxzZSwiaXNNb2JpbGVMb2dpbiI6ZmFsc2UsImlzTXlHb3ZMb2dpbiI6bnVsbCwidG9rZW4iOm51bGx9fQ.Q6un0qzsl7JUPR55XAUfWNuQF_XGvVIbaTzD0rFjuR1DTgIaMhIwvNAqU_jNPo_pvnRu524LTdxG-hVeNx2v_g";
//        String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDAwMDc3NzIiLCJwZXJtaXNzaW9ucyI6WyJBRERfU0VSVklDRVMiLCJFRElUX0NJVElaRU5fQ0hBUlRFUiIsIk9GRkxJTkVfR1JJRVZBTkNFX1VQTE9BRCIsIkVESVRfU0VSVklDRVMiLCJBRERfUFVCTElDX0dSSUVWQU5DRVMiLCJBRERfT0ZGSUNJQUxfR1JJRVZBTkNFUyIsIlZJRVdfU1VHR0VTVElPTiIsIlZJRVdfUFVCTElDX0dSSUVWQU5DRVMiLCJWSUVXX1JFR0lTVEVSIiwiVklFV19TVEFGRl9HUklFVkFOQ0VTIiwiVklFV19PRkZJQ0lBTF9HUklFVkFOQ0VTIiwiQUREX1NUQUZGX0dSSUVWQU5DRVMiLCJWSUVXX0NJVElaRU5fQ0hBUlRFUiIsIlZJRVdfU0VSVklDRVMiLCJBRERfQ0lUSVpFTl9DSEFSVEVSIl0sInVzZXJfaW5mbyI6eyJ1c2VySWQiOjkyNTYwLCJ1c2VybmFtZSI6IjEwMDAwMDAwNzc3MiIsInVzZXJUeXBlIjoiT0lTRl9VU0VSIiwib2lzZlVzZXJUeXBlIjoiR1JPIiwiZ3JzVXNlclR5cGUiOm51bGwsIm9mZmljZUluZm9ybWF0aW9uIjp7Im9mZmljZUlkIjo1Njg0LCJvZmZpY2VOYW1lQmFuZ2xhIjoi4Kau4Ka-4Kan4KeN4Kav4Kau4Ka_4KaVIOCmkyDgpongpprgp43gppog4Ka24Ka_4KaV4KeN4Ka34Ka-IOCmrOCmv-CmreCmvuCmlyIsIm9mZmljZU5hbWVFbmdsaXNoIjoiU0hFRCIsIm9mZmljZU1pbmlzdHJ5SWQiOjM3LCJvZmZpY2VPcmlnaW5JZCI6ODIsIm5hbWUiOiLgpqEuIOCmruCni-CmgyDgppzgpr7gppXgpr_gprAg4Ka54KeL4Ka44KeH4KaoIOCmhuCmluCmqOCnjeCmpiIsImRlc2lnbmF0aW9uIjoi4Kav4KeB4KaX4KeN4Kau4Ka44Kaa4Ka_4KasIiwiZW1wbG95ZWVSZWNvcmRJZCI6MjI5MjYxLCJvZmZpY2VVbml0T3JnYW5vZ3JhbUlkIjoxMDIyMTQsImxheWVyTGV2ZWwiOjEsImdlb0RpdmlzaW9uSWQiOjMsImdlb0Rpc3RyaWN0SWQiOjE4fSwiaXNBcHBlYWxPZmZpY2VyIjp0cnVlLCJpc09mZmljZUFkbWluIjpmYWxzZSwiaXNDZW50cmFsRGFzaGJvYXJkVXNlciI6ZmFsc2UsImlzQ2VsbEdSTyI6ZmFsc2UsImlzTW9iaWxlTG9naW4iOmZhbHNlLCJpc015R292TG9naW4iOm51bGwsInRva2VuIjpudWxsfX0.Vxf_g9WT8aBuS_d26oVPwizXvuXxmjqRwfUf-I-zYLmX4CDtiNRPc5ur3dGjuC4ggkl8ZjB8ZUyh-v_utW5kmA";
        Cookie cookie = new Cookie(Constant.HEADER_STRING, JWT);
        cookie.setMaxAge(Constant.COOKIE_EXPIRATION_TIME);
        response.addCookie(cookie);

        String redirectUrl = request.getParameter("redirectUrl");
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            response.sendRedirect("/" + redirectUrl);
        } else {
            response.sendRedirect("/login/success");
        }

    }

    public static void addAuthenticationForMyGov(UserDetailsImpl userDetails,
                                                 HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        UserInformation userInformation;
        String name;
        Set<String> permissionNamesSet;
        try {
            permissionNamesSet = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            userInformation = userDetails.getUserInformation();
            name = userDetails.getUsername();

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        String deviceToken = request.getParameter("device_token");
        if (StringUtil.isValidString(deviceToken)) {
            FcmService fcmService = BeanUtil.bean(FcmService.class);
            fcmService.registerDeviceToken(deviceToken, name);
            userInformation.setIsMobileLogin(true);
        } else {
            userInformation.setIsMobileLogin(false);
        }
//        String userName = userInformation.getUsername();
        String JWT = constuctJwtToken(name, permissionNamesSet, userInformation);
//        String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDAwMTU0MTUiLCJwZXJtaXNzaW9ucyI6WyJBRERfU0VSVklDRVMiLCJFRElUX0NJVElaRU5fQ0hBUlRFUiIsIk9GRkxJTkVfR1JJRVZBTkNFX1VQTE9BRCIsIkVESVRfU0VSVklDRVMiLCJBRERfUFVCTElDX0dSSUVWQU5DRVMiLCJBRERfT0ZGSUNJQUxfR1JJRVZBTkNFUyIsIlZJRVdfU1VHR0VTVElPTiIsIlZJRVdfUFVCTElDX0dSSUVWQU5DRVMiLCJWSUVXX1JFR0lTVEVSIiwiVklFV19TVEFGRl9HUklFVkFOQ0VTIiwiVklFV19PRkZJQ0lBTF9HUklFVkFOQ0VTIiwiQUREX1NUQUZGX0dSSUVWQU5DRVMiLCJWSUVXX0NJVElaRU5fQ0hBUlRFUiIsIlZJRVdfU0VSVklDRVMiLCJBRERfQ0lUSVpFTl9DSEFSVEVSIl0sInVzZXJfaW5mbyI6eyJ1c2VySWQiOjIzNDUsInVzZXJuYW1lIjoiMTAwMDAwMDE1NDE1IiwidXNlclR5cGUiOiJPSVNGX1VTRVIiLCJvaXNmVXNlclR5cGUiOiJHUk8iLCJncnNVc2VyVHlwZSI6bnVsbCwib2ZmaWNlSW5mb3JtYXRpb24iOnsib2ZmaWNlSWQiOjkzLCJvZmZpY2VOYW1lQmFuZ2xhIjoi4Kac4KeH4Kay4Ka-IOCmquCnjeCmsOCmtuCmvuCmuOCmleCnh-CmsCDgppXgpr7gprDgp43gpq_gpr7gprLgp58sIOCmmuCmvuCmgeCmpuCmquCngeCmsCIsIm9mZmljZU5hbWVFbmdsaXNoIjoiT2ZmaWNlIG9mIHRoZSBEZXB1dHkgQ29tbWlzc2lvbmVyLCBDaGFuZHB1ciIsIm9mZmljZU1pbmlzdHJ5SWQiOjUsIm9mZmljZU9yaWdpbklkIjoxNiwibmFtZSI6IuCmruCni-CmueCmvuCmruCnjeCmruCmpiDgpobgpqzgpqbgp4HgprIg4Ka54Ka-4KaHIiwiZGVzaWduYXRpb24iOiLgpoXgpqTgpr_gprDgpr_gppXgp43gpqQg4Kac4KeH4Kay4Ka-IOCmquCnjeCmsOCmtuCmvuCmuOCmlSIsImVtcGxveWVlUmVjb3JkSWQiOjIzMjMsIm9mZmljZVVuaXRPcmdhbm9ncmFtSWQiOjg4NzEsImxheWVyTGV2ZWwiOjUsImdlb0RpdmlzaW9uSWQiOjIsImdlb0Rpc3RyaWN0SWQiOjksImdlb1VwYXppbGFJZCI6bnVsbH0sImlzQXBwZWFsT2ZmaWNlciI6dHJ1ZSwiaXNPZmZpY2VBZG1pbiI6ZmFsc2UsImlzQ2VudHJhbERhc2hib2FyZFVzZXIiOmZhbHNlLCJpc0NlbGxHUk8iOmZhbHNlLCJpc01vYmlsZUxvZ2luIjpmYWxzZX19.38LlQHwtyiPoQ7CLWuUnY79xF0zadxyu04pp574wEf1OD5WFKGcdnKq-AxAn2rxZTjkpzvtBE6xIR1nfLSy-iQ";
//        String JWT = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDAwMDAwMTc3MTQiLCJwZXJtaXNzaW9ucyI6WyJWSUVXX1BVQkxJQ19HUklFVkFOQ0VTIiwiVklFV19TVEFGRl9HUklFVkFOQ0VTIiwiQUREX1BVQkxJQ19HUklFVkFOQ0VTIl0sInVzZXJfaW5mbyI6eyJ1c2VySWQiOjk5MDcsInVzZXJuYW1lIjoiMTAwMDAwMDE3NzE0IiwidXNlclR5cGUiOiJPSVNGX1VTRVIiLCJvaXNmVXNlclR5cGUiOiJTRVJWSUNFX09GRklDRVIiLCJncnNVc2VyVHlwZSI6bnVsbCwib2ZmaWNlSW5mb3JtYXRpb24iOnsib2ZmaWNlSWQiOjkzLCJvZmZpY2VOYW1lQmFuZ2xhIjoi4Kac4KeH4Kay4Ka-IOCmquCnjeCmsOCmtuCmvuCmuOCmleCnh-CmsCDgppXgpr7gprDgp43gpq_gpr7gprLgp58sIOCmmuCmvuCmgeCmpuCmquCngeCmsCIsIm9mZmljZU5hbWVFbmdsaXNoIjoiT2ZmaWNlIG9mIHRoZSBEZXB1dHkgQ29tbWlzc2lvbmVyLCBDaGFuZHB1ciIsIm9mZmljZU1pbmlzdHJ5SWQiOjUsIm9mZmljZU9yaWdpbklkIjpudWxsLCJuYW1lIjoi4Ka24KeH4KaWIOCmruCnh-CmnOCmrOCmvuCmuSAtIOCmieCmsi0g4Ka44Ka-4Kas4KeH4Kaw4Ka_4KaoIiwiZGVzaWduYXRpb24iOiLgprjgprngppXgpr7gprDgp4Ag4KaV4Kau4Ka_4Ka24Kao4Ka-4KawIiwiZW1wbG95ZWVSZWNvcmRJZCI6OTg3OSwib2ZmaWNlVW5pdE9yZ2Fub2dyYW1JZCI6ODg3NywibGF5ZXJMZXZlbCI6NSwiZ2VvRGl2aXNpb25JZCI6MiwiZ2VvRGlzdHJpY3RJZCI6OSwiZ2VvVXBhemlsYUlkIjowfSwiaXNBcHBlYWxPZmZpY2VyIjpmYWxzZSwiaXNPZmZpY2VBZG1pbiI6ZmFsc2UsImlzQ2VudHJhbERhc2hib2FyZFVzZXIiOmZhbHNlLCJpc0NlbGxHUk8iOmZhbHNlLCJpc01vYmlsZUxvZ2luIjpmYWxzZX19.wcVxUsacNZMhNAm4-akjxiRmDg1mTcInl9vEsvs5sI7YoJ4hR9Dj2DyRk8L1CaKt_UiJCwFNC7P8SJ-oYJNGpA";

        Cookie cookie = new Cookie(Constant.HEADER_STRING, JWT);
        cookie.setMaxAge(Constant.COOKIE_EXPIRATION_TIME);
        response.addCookie(cookie);

//        String redirectUrl = request.getParameter("redirectUrl");
//        if (redirectUrl != null && !redirectUrl.isEmpty()) {
//            response.sendRedirect("/" + redirectUrl);
//        } else {
//            response.sendRedirect("/login/success");
//        }

        response.sendRedirect("/login/success");

    }

    public static String constuctJwtToken(String username, Set<String> permissionNames, UserInformation userInformation) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(Constant.AUTHORITY, permissionNames);
        claims.put(Constant.USER_INFO, userInformation);
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + Constant.EXPIRATIONTIME))
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, Constant.SECRET)
                .compact();
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String token = CookieUtil.getValue(request, Constant.HEADER_STRING);
        if (token == null) {
            token = request.getHeader(Constant.HEADER_STRING);
        }
        if (token != null) {
            token = token.replace(Constant.TOKEN_PREFIX, "");
            Claims body = Jwts.parser()
                    .setSigningKey(Constant.SECRET)
                    .parseClaimsJws(token)
                    .getBody();

            String username = body.getSubject();
            List<String> permissions = (ArrayList<String>) body.get(Constant.AUTHORITY);

            UserInformation userInformation = objectMapper.convertValue(body.get(Constant.USER_INFO), UserInformation.class);

            //        OfficeInformation officeInformation = OfficeInformation.builder()
//                .officeId((long)2164)
//                .officeOriginId((long)185)
//                .officeMinistryId((long)49)
//                .officeNameBangla("প্রবাসী কল্যাণ ও বৈদেশিক কর্মসংস্থান মন্ত্রণালয়")
//                .officeNameEnglish("Ministry of Expatriate Welfare & Overseas Employment")
//                .designation("অতিরিক্ত সচিব")
//                .employeeRecordId((long)90673)
//                .name("মোঃ জয়নাল আবেদীন মোল্লা")
//                .officeUnitOrganogramId((long)78915)
//                .layerLevel(Long.valueOf(1))
//                .geoDivisionId(Long.valueOf(3))
//                .geoDistrictId(Long.valueOf(18))
//                .geoUpazilaId(Long.valueOf(0))
//                .build();
//
//        userInformation = UserInformation
//                .builder()
//                .userId(Long.parseLong("100000007435"))
//                .username("মোঃ জয়নাল আবেদীন মোল্লা")
//                .userType(UserType.OISF_USER)
//                .oisfUserType(OISFUserType.SERVICE_OFFICER)
//                .grsUserType(null)
//                .officeInformation(officeInformation)
//                .isAppealOfficer(true)
//                .isOfficeAdmin(false)
//                .isCentralDashboardUser(true)
//                .isCellGRO(false)
//                .isMobileLogin(false)
//                .build();

            List<GrantedAuthorityImpl> grantedAuthorities = permissions.stream()
                    .map(permission -> {
                        return GrantedAuthorityImpl.builder()
                                .role(permission)
                                .build();
                    }).collect(Collectors.toList());

            return username != null ?
                    new CustomAuthenticationToken(username, null, grantedAuthorities, userInformation) :
                    null;
        }
        return null;
    }

    public static Authentication getMyGovAuthentication(HttpServletRequest request, String token) {
        token = token.replace(Constant.TOKEN_PREFIX, "");
        Claims body = Jwts.parser()
                .setSigningKey(Constant.SECRET)
                .parseClaimsJws(token)
                .getBody();

        String username = body.getSubject();
        List<String> permissions = (ArrayList<String>) body.get(Constant.AUTHORITY);

        UserInformation userInformation = objectMapper.convertValue(body.get(Constant.USER_INFO), UserInformation.class);

        List<GrantedAuthorityImpl> grantedAuthorities = permissions.stream()
                .map(permission -> {
                    return GrantedAuthorityImpl.builder()
                            .role(permission)
                            .build();
                }).collect(Collectors.toList());

        return username != null ?
                new CustomAuthenticationToken(username, null, grantedAuthorities, userInformation) :
                null;
    }

}

