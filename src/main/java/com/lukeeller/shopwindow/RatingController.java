package com.lukeeller.shopwindow;

import com.google.cloud.automl.v1beta1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class RatingController {

    @GetMapping("/rating")
    public String newRating(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "rating/new";
    }

    @PostMapping("/rating")
    public String obtainRating(@RequestParam("file") MultipartFile file, Model model) throws IOException {
// Create client for prediction service.
        PredictionServiceClient predictionClient = PredictionServiceClient.create();

        // Get full path of model
        ModelName name = ModelName.of("hottodoggude-wanai", "us-central1", "ICN4603688265246652448");


        // Set the payload by giving the content and type of the file.
        Image image =
                Image.newBuilder().setImageBytes(ByteString.copyFrom(file.getBytes())).build();

        ExamplePayload payload = ExamplePayload.newBuilder().setImage(image).build();


        // params is additional domain-specific parameters.
        // currently there is no additional parameters supported.
        Map<String, String> params = new HashMap<String, String>();
        PredictResponse response = predictionClient.predict(name, payload, params);

        final Optional<AnnotationPayload> good = response.getPayloadList()
                .stream()
                .filter(annotationPayload -> annotationPayload.getDisplayName().equals("good"))
                .findFirst();

        final Optional<AnnotationPayload> bad = response.getPayloadList()
                .stream()
                .filter(annotationPayload -> annotationPayload.getDisplayName().equals("bad"))
                .findFirst();

        if (good.isPresent()) {
            model.addAttribute("good", Math.round(good.get().getClassification().getScore() * 100));
        } else {
            model.addAttribute("good", 0);
        }

        if (bad.isPresent()) {
            model.addAttribute("bad", Math.round(bad.get().getClassification().getScore() * 100));
        } else {
            model.addAttribute("bad", 0);
        }

        return "rating/rating";
    }

}
